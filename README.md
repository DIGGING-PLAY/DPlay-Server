# DPlay-Server

DPlay 서버 애플리케이션

## 🚀 시작하기

### 필요 사항
- Java 17 이상
- MySQL 8.0 이상
- Gradle 8.5 이상

### 설치 및 실행

#### 로컬 개발 환경

1. RDS 터널링 설정 (SSH 포트 포워딩)
```bash
ssh -i /path/to/your.pem -L 3307:d-play.c12amqowqa8w.ap-northeast-2.rds.amazonaws.com:3306 ubuntu@3.38.79.157
```

2. AWS S3 환경 변수 설정
```bash
export AWS_ACCESS_KEY=your_access_key
export AWS_SECRET_KEY=your_secret_key
```

3. 애플리케이션 실행 (로컬 프로필)
```bash
./gradlew bootRun
# 또는
java -jar build/libs/DPlay-Server-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

#### 프로덕션 환경

1. 환경 변수 설정
```bash
export DB_USERNAME=dplay
export DB_PASSWORD=D.PLAY.zzang!1
export JWT_SECRET_KEY=your_jwt_secret_key
export AWS_ACCESS_KEY=your_aws_access_key
export AWS_SECRET_KEY=your_aws_secret_key
```

2. 애플리케이션 실행 (프로덕션 프로필)
```bash
java -jar build/libs/DPlay-Server-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

애플리케이션이 `http://localhost:8080/api`에서 실행되며, `/api/health` 엔드포인트로 상태 확인이 가능합니다.

## 🔍 API 엔드포인트

### Health Check
- `GET /api/health` - 서버 상태 확인
- `GET /actuator/health` - Spring Actuator Health Check

## 🛠 기술 스택
- Spring Boot 3.4.3
- Java 17
- MySQL 8.0 (AWS RDS)
- Gradle 8.5
- AWS S3 (파일 저장)
- JWT (인증)

## 📦 빌드
```bash
./gradlew build
```

## 🧪 테스트
```bash
./gradlew test
```

## 📝 라이선스
이 프로젝트는 MIT 라이선스를 따릅니다.

// touch
