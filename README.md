# EquipSync

## Create a postgresql container for local development

```sh
docker run -d -v "C:\\Users\Mitul\OneDrive\Desktop\VScode\Full Stack Projects/EquipSync\PgData":/var/lib/postgresql -e POSTGRES_USERNAME=postgres -e POSTGRES_PASSWORD=12345 -p 5432:5432 --name EquipSync-Postgre  postgres
```