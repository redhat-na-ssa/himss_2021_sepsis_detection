FHIR_SERVER_URL=https://$(oc get route fhir-server -n user1-sepsisdetection --template='{{ .spec.host }}')

echo $FHIR_SERVER_URL
sleep 5;

for i in {1..30}; do
    curl -X POST \
       -H "Content-Type:application/fhir+json" \
       $FHIR_SERVER_URL/fhir \
       -d "@sepsisdetection-rhpam/src/test/resources/fhir/DemoBundle.json"

    sleep 5;
done
