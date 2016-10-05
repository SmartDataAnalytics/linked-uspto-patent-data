# linked-uspto-patent-data
The work flow of extracting and triplifying the patents data is done as folllowing:

## 1-Downloading:
In downloading patents data, a set of files is used where each file contains the URLs of the patents data for a specific year. A bash
file named **downloadFiles.sh** is used to download the patents data. It tworks by reading URLs from the year file and create a folder named after
the year inside a specified destination folder where the downloadded zipped files will be stored. The syntax is described as:

*bash downloadFiles.sh Path-To-URLs-Of-Year Destination-Folder*
***Note***: Inside the *Destination-Folder* a folder with the year name is created to download the zipped files in it.

> *Example:* bash downloadFiles.sh /home/../2006 /home/../patentsDownloadedZipped/ , inside patentsDownloadedZipped folder named 2006 will be created.

## 2-Unzipping and splitting:
In this step, the zipped downloaded files are extracted and each XML file is splitted into multiple XML files. Each new XML file contains only an individual
patent data. Another bash file is used to perform this process named **splitter.sh**. The syntax to use the bash file is:
*bash splitter.sh Path-To-Year-Folder Destination-Folder*

> *Example:* bash splitter.sh /home/../patentsDownloadedZipped/2006/ /home/../patentsSplitted/2006/

## 3-Processing:
A processing step is performed for the individual patents files to be enriched. The additional data are descriptions related to the classifications codes. The codes for the role, kind, country, state and city are downloaded and saved as text files directly without using the jar. This is done by using the jar file **XMLProcessorV2.jar**. This jar performs different functionalities based on a given
option. These functionalities are:

* Extracting the codes and their descriptions from raw pages(HTML, XML,downloaded files...etc)

**java -jar XMLProcessorV2.jar ext classification-Type Raw-Code-Folder Structured-Code-Folder Missed-Code-Folder**

*ext: The option to select the extraction functionality*
*classification-Type: It specifies the type of classification to extract its descriptions. Its value is either **ipc** for international classification code or **us**  for the US classification (national)*
*Raw-Code-Folder: The folder specifies the raw classificatin code files. There exist two types of folders based on the type of classification and differ in their contents. In case of **us** classification, the folder contains the top-level classification files where each file named after a top level code. Inside each file the sub-classes codes and descriptions. In case of **ipc** classification, the folder contains the files including different codes and their descriptions in xml formats*
*Structured-Code-Folder: The folder where the pairs code-description files are stored*
*Missed-Code-Folder: The folder where missed codes are stored for later review. It is useful in case of **us** classification*

> *Example:* java -jar XMLProcessorV2.jar ext ipc/us /home/../IPC_Codes_XML/ /home/../IPCCodes

* Processing the patents files

**java -jar XMLProcessorV2.jar pro Splitted-Patents-Folder-Year Processed-Patents-Folder IPCCode-Folder USCode-Folder MissedCodes-Folder**

 -*pro: the option to process the patents files*
 -*Splitted-Patents-Folder-Year: the yearly patents folder*
 -*Processed-Patents-Folder: the folder containing the processed xml patents files*
 -*IPCCode-Folder: the folder contains the IPC codes*
 -*USCode-Folder: the folder contains the us codes*
 -*MissedCodes-Folder: the folder contains several sub-folders based on year where each folder includes a per-XMLfile text file for all types of missed codes *
**java -jar XMLProcessorV2.jar pro /home/../patentsSplitted/2005/ /home/../patentsProcessed/2005/ /home/../IPCCodes/ /home/../US/ /home/../MissedCodes/2005/**
*Note: that the codes files must be in the same foder with the XMLProcessorV2.jar*

* Classification of files with missed codes detected during processing the xml files based on the code type
**java -jar XMLProcessorV2.jar classify MissedCodes-Folder MissedCodes-Classified-Folder Code-Type-EXpression**
*classify: the option to classification*
*MissedCodes-Folder: the folder contains the missed codes files per year*
*MissedCodes-Classified-Folder: the folder contains the classified missed codes per year*
*Code-Type-EXpression: the expression specifies the type of code in interest to differentiate between files that contain that missed code or not*
**java -jar XMLProcessorV2.jar classify /home/../MissedCodes/2005/ /home/../Classified-MissedCodes/2005/ /us-patent-grant/us-bibliographic-data-grant/parties/agents/agent/addressbook/orgname**

## 4-Triplifying
It is the targeted step in the workflow where the XML files are transformed into RDF data. This is executed by using RML Mapping [http://rml.io/] and RML Processing [https://github.com/mmlab/RMLValidator/tree/rdfunit]. The syntax is:
**java -jar RMLMMF.jar RML-Mapping-File Processed-Patents-Folder**
> *Example: *java -jar RMLMMF.jar /home/../mapping.ttl  /home/../patentsProcessed/2014/
