# 2024 LKPR Distribution

To deploy this distribution:
- Set `SHARED_FOLDER` to the data folder containing csv files and notebooks. By default `SHARED_FOLDER=./Data`.
- To ensure jupyter lab has permissions for files in `SHARED_FOLDER` set the `SCIPY_USER_ID` (default `1000`) and 
  `SCIPY_USER_GROUP` (default `users`) to the user id of the owner `SHARED_FOLDER` and the owner's group respectively.  
- execute `docker-compose up`

## Running Behind Apache Proxy
Add a location to virtual host in `httpd-vhosts.conf`:
```
<VirtualHost *:80>
    <Location APP_ROOT_PATH>
        ProxyPass http://localhost:1234 nocanon upgrade=websocket
        ProxyPassReverse http://localhost:1234
    </Location>
</VirtualHost>    
```

Change the path replace the placeholder `APP_ROOT_PATH` with the value of the variable with the same name `APP_ROOT_PATH`
in `.evn` file, e.g. `/lkpr`. 

The `upgrade=websocket` in the `ProxyPass` directive is necessary for the scipy service allowing to run jupyter notebooks: