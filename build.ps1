# 构建脚本
Write-Host "===== 构建项目 =====" -ForegroundColor Cyan

$ErrorActionPreference = "Stop"

# 下载 Qdrant 镜像
Write-Host "`n[1/3] 下载 Qdrant 镜像..." -ForegroundColor Yellow
docker pull qdrant/qdrant

# Python 依赖安装
Write-Host "`n[2/3] 安装 Python 依赖..." -ForegroundColor Yellow
conda activate ML
pip install thrift
pip install face_recognition

# Java 构建
Write-Host "`n[3/3] 构建 Java 项目..." -ForegroundColor Yellow
mvn -f src/java/pom.xml clean compile

Write-Host "`n===== 构建完成 =====" -ForegroundColor Green
