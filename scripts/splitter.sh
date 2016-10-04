#!/bin/bash
dir="$1"     #Path to the directory contains the zipped dump files of patents
newdir="$2"       #"/media/mofeed/A0621C46621C24164/03_Work/patentsFiles/2010/filesToConvert/"     #"/media/mofeed/A0621C46621C24164/03_Work/patentsFiles/2010/test/"
newfile=""

#check number of parameters
if [ "$#" -ne 2 ]; then
    echo "Illegal number of parameters:  bash_file  source_folder  destination_folder"
else
    case "$newdir" in */)    #the folder path is correct that ends with slash
		#unzip the files and extract the xml
		echo "Unzip files....."
		
		for zipfile in "$dir"/*.zip; do
		   unzip -o "$zipfile" -d "$dir"  # unzip the file with options overwrite existing files without prompting in the optional directory to which to extract files
		done
		
		echo "Formating xml files....."
		
		#reformat the xml file in ideal form (eah tag in a line
		find "$dir" -name "*.xml" -type f -exec xmllint --output '{}' --format '{}' \;  #using xmllint tool to reformat the xml file one tag per line
		
		echo "Splitting patents files....."
		
		for fil in "$dir"*.xml; do
			s=${fil##*/}
			csplit -kszf "$newdir"${s%.*}"_" -b "%04d.xml" "$dir"${fil##*/} /\<\?xml.*/ "{10000}" 2>/dev/null
		done
    ;;
*)       # the folder path does not end with slash
    echo "Destinaion folder path does not have slash at the end"
    ;;
esac
    
fi
