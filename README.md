# postgres-long-object
spring mvc async example with postgres-long-object and jakewharton DiskLruCache 

#make sure you have postgres on 
docker run --storage-opt dm.basesize=20G  --name myPostgres -it -p 5432:5432  -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=password -e POSTGRES_DB=hpmc -d postgres:9.6 
