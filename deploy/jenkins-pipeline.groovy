pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '10'))
  }

  environment {
    APP_NAME = 'mcpforge'
    IMAGE_NAME = 'mcpforge'
    DEPLOY_DIR = '/data/xxx/projects/mcpforge'
    COMPOSE_FILE = '/data/xxx/projects/mcpforge/docker-compose.yml'
    DOCKER_NETWORK = 'xxx-network'
  }

  stages {
    stage('Validate Deploy Env') {
      steps {
        sh '''
          set -eu

          if [ ! -f "$DEPLOY_DIR/.env" ]; then
            echo "缺少生产环境配置文件：$DEPLOY_DIR/.env"
            echo "请先根据 deploy/.env.example 创建并填写真实配置。"
            exit 2
          fi

          if ! docker network inspect "$DOCKER_NETWORK" >/dev/null 2>&1; then
            echo "缺少 Docker 网络：$DOCKER_NETWORK"
            echo "请先执行：docker network create $DOCKER_NETWORK"
            exit 2
          fi

          test -w "$DEPLOY_DIR"
        '''
      }
    }

    stage('Checkout') {
      steps {
        deleteDir()
        checkout([
          $class: 'GitSCM',
          branches: [[name: 'main']],
          userRemoteConfigs: [[
            url: 'YOUR_GIT_REPO_URL'
          ]]
        ])
      }
    }

    stage('Build Image') {
      steps {
        sh '''
          set -eu
          docker build \
            -t "$IMAGE_NAME:$BUILD_NUMBER" \
            -t "$IMAGE_NAME:latest" \
            .
        '''
      }
    }

    stage('Prepare Deploy Files') {
      steps {
        sh '''
          set -eu
          cp deploy/docker-compose.yml "$COMPOSE_FILE"
        '''
      }
    }

    stage('Deploy') {
      steps {
        sh '''
          set -eu
          cd "$DEPLOY_DIR"
          MCPFORGE_IMAGE="$IMAGE_NAME:$BUILD_NUMBER" docker compose up -d
          docker compose ps

          if [ "$(docker inspect -f '{{.State.Running}}' "$APP_NAME")" != 'true' ]; then
            echo "$APP_NAME 容器未正常运行"
            docker logs --tail 100 "$APP_NAME" || true
            exit 1
          fi
        '''
      }
    }
  }

  post {
    success {
      echo 'MCPForge Single 部署完成：http://YOUR_SERVER_HOST:8090/admin'
    }
    failure {
      echo 'MCPForge Single 部署失败，请查看 Jenkins 控制台日志。'
    }
  }
}
