pipeline {
  agent any

  options {
    timestamps()
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '10'))
  }

  environment {
    APP_NAME = 'mcpforge-admin-web'
    IMAGE_NAME = 'mcpforge-admin-web'
    DEPLOY_DIR = '/data/xxx/projects/mcpforge-admin-web'
    COMPOSE_FILE = '/data/xxx/projects/mcpforge-admin-web/docker-compose.yml'
    DOCKER_NETWORK = 'xxx-network'
  }

  stages {
    stage('Validate Deploy Env') {
      steps {
        sh '''
          set -eu

          if ! docker network inspect "$DOCKER_NETWORK" >/dev/null 2>&1; then
            echo "缺少 Docker 网络：$DOCKER_NETWORK"
            echo "请先在服务器执行：docker network create $DOCKER_NETWORK"
            exit 2
          fi

          mkdir -p "$DEPLOY_DIR"
          if [ ! -w "$DEPLOY_DIR" ]; then
            echo "Jenkins 没有部署目录写权限：$DEPLOY_DIR"
            echo "请在服务器调整该目录的属主和权限后重试。"
            exit 3
          fi
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
        dir('admin-web') {
          sh '''
            set -eu
            docker build \
              -t "$IMAGE_NAME:$BUILD_NUMBER" \
              -t "$IMAGE_NAME:latest" \
              .
          '''
        }
      }
    }

    stage('Prepare Deploy Files') {
      steps {
        sh '''
          set -eu
          cp admin-web/deploy/docker-compose.yml "$COMPOSE_FILE"
        '''
      }
    }

    stage('Deploy') {
      steps {
        sh '''
          set -eu
          cd "$DEPLOY_DIR"
          MCPFORGE_ADMIN_WEB_IMAGE="$IMAGE_NAME:$BUILD_NUMBER" docker compose up -d
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
      echo 'MCPForge Admin Web 部署完成：http://YOUR_SERVER_HOST:8091/'
    }
    failure {
      echo 'MCPForge Admin Web 部署失败，请查看 Jenkins 控制台日志。'
    }
  }
}
