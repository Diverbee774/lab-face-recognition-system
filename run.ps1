# 启动脚本
Write-Host "===== 启动服务 =====" -ForegroundColor Cyan

# 启动 Python 服务
Write-Host "`n[1/2] 启动 Python Thrift 服务 (localhost:9090)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "conda activate ML; python src/py/main.py"

# 等 Python 服务启动
Start-Sleep -Seconds 3

# 启动 Java 服务
Write-Host "`n[2/2] 启动 Java Spring Boot (localhost:8080)..." -ForegroundColor Yellow
mvn -f src/java/pom.xml spring-boot:run

Write-Host "`n===== 服务启动中 =====" -ForegroundColor Green
Write-Host "Python: localhost:9090" -ForegroundColor Cyan
Write-Host "Java:   localhost:8080" -ForegroundColor Cyan
