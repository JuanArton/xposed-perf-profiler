#!/system/bin/sh

FILE_PATH="$1/$2"
CONTENT="$3"


chmod 644 "$FILE_PATH"

echo "$CONTENT" > "$FILE_PATH"

chmod 444 "$FILE_PATH"
