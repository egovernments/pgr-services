#!/bin/sh
#acme-client -a https://letsencrypt.org/documents/LE-SA-v1.2-November-15-2017.pdf -Nnmv $DOMAIN && renew=1
sed -e "s/DOMAIN/$DOMAIN/g" -e "s#ASSET_BUCKET_URL#$ASSET_BUCKET_URL#g" /tmp/default_ssl.conf > /etc/nginx/conf.d/default_ssl.conf
if [[ "$SUBFILTER" = "YES" ]]; then
echo "sub_filter  '<head>' '" >> /etc/nginx/conf.d/sub_filter.conf
  if [[ -n "$ULB_JS_URL" ]]; then
    echo "<script src="$ULB_JS_URL" type="text/javascript"></script>" >> /etc/nginx/conf.d/sub_filter.conf
  fi

  if [[ -n "$TELEMETRY_JS_URL" ]]; then
    echo "<script src="$TELEMETRY_JS_URL" type="text/javascript"></script>" >> /etc/nginx/conf.d/sub_filter.conf
  fi
  
  if [[ -n "$CHATBOT_USERINFO_GEN_JS_URL" ]]; then
    echo "<script src="$CHATBOT_USERINFO_GEN_JS_URL" type="text/javascript"></script>" >> /etc/nginx/conf.d/sub_filter.conf
  fi
  
echo "<"/"head>';" >> /etc/nginx/conf.d/sub_filter.conf
echo "sub_filter_once on;" >> /etc/nginx/conf.d/sub_filter.conf
fi
echo "$JAEGER_CREDENTIALS" > /etc/nginx/.jaegerhtpasswd
nginx -g "daemon off;"
