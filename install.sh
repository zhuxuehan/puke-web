vim /etc/docker/daemon.json
sudo systemctl restart docker
docker pull mysql:5.7
docker run -p 3306:3306 --name mysql -v /mydata/mysql/log:/var/log/mysql -v /mydata/mysql/data:/var/lib/mysql -v /mydata/mysql/conf:/etc/mysql  -e MYSQL_ROOT_PASSWORD='Hello123!@#'  -d mysql:5.7
docker pull redis:latest
docker run -itd --name redis -p 6379:6379 redis
docker build -t puke .
docker run -p 80:80 -d puke