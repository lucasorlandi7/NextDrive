#!/bin/bash
# Next Drive - backup e restauração do banco.
# Uso:  ./10_backup_restore.sh backup
#       ./10_backup_restore.sh restaurar arquivo.backup
# Se a porta ou o usuário forem diferentes, mude as variáveis abaixo.
# A senha do PostgreSQL pode ser informada antes:  export PGPASSWORD=suasenha

BANCO=nextdrive
RESTAURADO=nextdrive_restaurado
USUARIO=postgres
HOST=localhost
PORTA=${PORTA:-5432}
PASTA=./backups

case "$1" in
    backup)
        mkdir -p "$PASTA"
        ARQUIVO="$PASTA/${BANCO}_$(date +%Y%m%d_%H%M%S).backup"
        pg_dump -h "$HOST" -p "$PORTA" -U "$USUARIO" -Fc -f "$ARQUIVO" "$BANCO" && echo "Backup salvo em $ARQUIVO"
        ;;
    restaurar)
        if [ -z "$2" ]; then
            echo "Informe o arquivo de backup."
            exit 1
        fi
        dropdb -h "$HOST" -p "$PORTA" -U "$USUARIO" --if-exists "$RESTAURADO"
        createdb -h "$HOST" -p "$PORTA" -U "$USUARIO" "$RESTAURADO"
        pg_restore -h "$HOST" -p "$PORTA" -U "$USUARIO" -d "$RESTAURADO" "$2" && echo "Banco $RESTAURADO restaurado a partir de $2"
        ;;
    *)
        echo "Uso: $0 backup | $0 restaurar arquivo.backup"
        exit 1
        ;;
esac
