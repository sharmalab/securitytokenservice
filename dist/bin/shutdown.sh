# stop daemon    
pid=`ps uax | grep STS_INSTANCE | head -n 1 | awk '{print $2}'`   
echo "Killing $pid"
kill -9 $pid