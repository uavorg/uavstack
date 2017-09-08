#!/bin/sh
isSudo=""
if [[ $(whoami) != "root" ]] ; then isSudo="sudo" ;  fi
version=$("python" --version 2>&1 | awk '{print $2 }')
if [[ "$version" > "3.0" && "$version" < "4.0" ]]; then
    echo $($isSudo "python" networkIoDetect3.py $* )
elif [[ "$version" > "2.6" && "$version" < "3.0" ]]; then
    echo $($isSudo "python" networkIoDetect2.py $* )
else
    echo "shellscript error"
fi