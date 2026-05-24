@echo off
echo === 启动 MySQL 数据库 ===
start "" /B "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe" --datadir="C:\ProgramData\MySQL\MySQL Server 8.4\Data"
echo MySQL 已启动（后台运行）
echo.
echo 用完可以直接关闭此窗口，MySQL 仍在后台运行。
echo 如需彻底关闭，打开任务管理器结束 mysqld 进程。
pause
