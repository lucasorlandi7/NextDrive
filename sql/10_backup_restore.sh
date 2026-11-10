#!/usr/bin/env bash
# =====================================================================
# Next Drive - 10_backup_restore.sh : rotina de backup e restauração (RF18)
#
# Uso:
#   ./sql/10_backup_restore.sh backup               backup completo do banco (formato custom)
#   ./sql/10_backup_restore.sh globais              backup dos usuários/grupos (roles) do servidor
#   ./sql/10_backup_restore.sh listar               lista os backups existentes
#   ./sql/10_backup_restore.sh verificar <arquivo>  confere se o arquivo de backup está íntegro
#   ./sql/10_backup_restore.sh restore <arquivo> [banco_destino]
#                                                   restaura em um banco NOVO (padrão: next_drive_restore)
#
# Variáveis de ambiente (todas opcionais):
#   DB_NAME (next_drive)  DB_USER (postgres)  DB_HOST (localhost)  DB_PORT (5432)
#   BACKUP_DIR (./backups)  RETENCAO_DIAS (7)
#   A senha deve vir de PGPASSWORD ou do arquivo ~/.pgpass (nunca escreva a senha aqui).
#
# Formato custom (-Fc): comprimido e permite restauração seletiva de tabelas
# com pg_restore -t (RNF05 do exemplo de referência).
# =====================================================================
set -euo pipefail

DB_NAME="${DB_NAME:-next_drive}"
DB_USER="${DB_USER:-postgres}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
RETENCAO_DIAS="${RETENCAO_DIAS:-7}"
BACKUP_DIR="${BACKUP_DIR:-$(cd "$(dirname "$0")/.." && pwd)/backups}"

CONEXAO=(-h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER")

uso() {
    sed -n '2,17p' "$0" | sed 's/^# \{0,1\}//'
    exit 1
}

backup() {
    mkdir -p "$BACKUP_DIR"
    local arquivo="$BACKUP_DIR/${DB_NAME}_$(date +%Y%m%d_%H%M%S).dump"
    echo "Gerando backup de '$DB_NAME' em $arquivo ..."
    pg_dump "${CONEXAO[@]}" -d "$DB_NAME" -Fc -f "$arquivo"
    verificar "$arquivo"
    # política de retenção: remove backups mais antigos que RETENCAO_DIAS dias
    find "$BACKUP_DIR" -name "${DB_NAME}_*.dump" -type f -mtime +"$RETENCAO_DIAS" -print -delete \
        | sed 's/^/Removido (retenção): /'
    echo "Backup concluído: $arquivo ($(du -h "$arquivo" | cut -f1))"
}

globais() {
    mkdir -p "$BACKUP_DIR"
    local arquivo="$BACKUP_DIR/roles_$(date +%Y%m%d_%H%M%S).sql"
    pg_dumpall "${CONEXAO[@]}" --roles-only -f "$arquivo"
    echo "Usuários e grupos salvos em $arquivo"
}

listar() {
    ls -lh "$BACKUP_DIR" 2>/dev/null || echo "Nenhum backup em $BACKUP_DIR"
}

verificar() {
    local arquivo="${1:?informe o arquivo de backup}"
    [ -f "$arquivo" ] || { echo "Arquivo não encontrado: $arquivo"; exit 1; }
    pg_restore --list "$arquivo" > /dev/null
    echo "Verificação OK: $(pg_restore --list "$arquivo" | grep -c ' TABLE public ') tabelas no backup."
}

restaurar() {
    local arquivo="${1:?informe o arquivo de backup}"
    local destino="${2:-${DB_NAME}_restore}"
    verificar "$arquivo"
    if psql "${CONEXAO[@]}" -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = '$destino'" | grep -q 1; then
        echo "O banco '$destino' já existe. Escolha outro nome ou apague-o antes (DROP DATABASE)."
        exit 1
    fi
    echo "Criando banco '$destino' e restaurando $arquivo ..."
    createdb "${CONEXAO[@]}" "$destino"
    pg_restore "${CONEXAO[@]}" -d "$destino" --exit-on-error "$arquivo"
    echo "Restauração concluída em '$destino'. Conferência rápida:"
    psql "${CONEXAO[@]}" -d "$destino" -tAc \
        "SELECT 'tabelas: ' || count(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE'"
}

case "${1:-}" in
    backup)    backup ;;
    globais)   globais ;;
    listar)    listar ;;
    verificar) verificar "${2:-}" ;;
    restore)   restaurar "${2:-}" "${3:-}" ;;
    *)         uso ;;
esac
