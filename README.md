# gpmail-contacts-sync

## Running from pre-built image

```shell
docker run \
  -it \
  --rm \
  -p 8080:8080 \
  hwangkevin/gpmail-contacts-sync:0.0.1 \
  --app.google-oauth2.clientId=FILL_ME_IN \
  --app.google-oauth2.clientSecret=FILL_ME_IN
```

You can also supply a Tribe API key w/ the optional command-line arg `--app.tribe.api-key` to fetch photos from Tribe as fallback.
