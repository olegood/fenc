keytool -genseckey \
  -alias kek-YYYY-MM \
  -keyalg AES \
  -keysize 256 \
  -keystore /opt/app/secrets/kek.p12 \
  -storetype PKCS12 \
  -storepass <KEK_STORE_PASSWORD> \
  -keypass <KEK_STORE_PASSWORD>
