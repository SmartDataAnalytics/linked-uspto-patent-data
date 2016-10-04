#!/bin/bash

# Create an array files that contains list of filenames
fpath="$1" #parameter 1: The path of the file that contains a list dumps' URLs of patents
dpath="$2" #parameter 2: The path of the folder where the downloaded files will be saved (must end with slash)
readyear=""
#check the number of parameters

if [ "$#" -ne 2 ]; then
    echo "Illegal number of parameters:  bash_file  file_of_URLs_to_download  destination_folder"
else
    case "$dpath" in
*/)
    while read -r uri; do
         readyear=$(echo "${uri}" | sed  -r 's/.+\/([0-9]+)\/.*/\1/') #extract the year
         if [ ! -d "$dpath""$readyear" ]; then # Does a folder named after this year exist.
            mkdir "$dpath""$readyear"   # create  a folder named after extracted year if $DIRECTORY doesn't exist.
         fi
         wget wget -P "${dpath}""$readyear" "${uri}" #download the dump file
   done < "$fpath"
    ;;
*)
    echo "Destinaion folder path does not have slash at the end"
    ;;
esac
    
fi

