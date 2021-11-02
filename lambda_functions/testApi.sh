#Define base API URL
BASE_URL="https://i89ssvhs3d.execute-api.us-west-1.amazonaws.com/Prod"

#Define endpoints
ENDPOINT1="getLogMessages"
ENDPOINT2="checkLogPresence"

#Define params
TIME="01:10:23.342"
DELTA="00:00:02.000"

#Run CURL to test the endpoints
printf "Running ${ENDPOINT1} with GET request\n"
curl  $BASE_URL/$ENDPOINT1/$TIME/$DELTA
printf "\nRunning ${ENDPOINT2} with GET request\n"
curl  $BASE_URL/$ENDPOINT2/$TIME/$DELTA
printf "\nRunning ${ENDPOINT1} with POST request\n"
curl -d "time=${TIME}&delta=${DELTA}" -H  "Content-Type: application/x-www-form-urlencoded"   -X POST  $BASE_URL/$ENDPOINT1
printf "\nRunning ${ENDPOINT2} with POST request\n"
curl -d "time=${TIME}&delta=${DELTA}" -H  "Content-Type: application/x-www-form-urlencoded"   -X POST  $BASE_URL/$ENDPOINT2
