# 启动脚本
Write-Host "===== 启动服务 =====" -ForegroundColor Cyan

# 启动 Python 服务
Write-Host "`n[1/3] 启动 Python Thrift 服务 (localhost:9090)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "conda activate ML; python src/py/main.py"

# 启动 Java 服务
Write-Host "`n[2/3] 启动 Java Spring Boot (localhost:8080)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "mvn -f src/java/pom.xml spring-boot:run"

# 启动 Vue 前端
Write-Host "`n[3/3] 启动 Vue 前端 (localhost:5173)..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd src/frontend; npm run dev"

Write-Host "`n===== 服务启动中 =====" -ForegroundColor Green
Write-Host "Python:  localhost:9090" -ForegroundColor Cyan
Write-Host "Java:    localhost:8080" -ForegroundColor Cyan
Write-Host "Vue:     localhost:5173" -ForegroundColor Cyan