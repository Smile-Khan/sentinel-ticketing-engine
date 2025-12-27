#!/bin/bash

# Target Seat ID (Change this to a valid ID from your database, e.g., 1)
SEAT_ID=2
# Number of concurrent requests
CONCURRENCY=50

echo "🚀 Starting Flash Sale Stress Test for Seat ID: $SEAT_ID"
echo "Simulating $CONCURRENCY users clicking 'Reserve' simultaneously..."

# This loop sends 50 requests in the background (&) as fast as possible
for i in {1..50}
do
   curl -X POST "http://localhost:8080/api/v1/bookings/reserve/$SEAT_ID" \
        -H "Content-Type: application/json" \
        -s -o /dev/null -w "Request $i: %{http_code}\n" &
done

# Wait for all background processes to finish
wait

echo "✅ Stress test complete. Check your IDE logs to see the Redis Lock in action!"