# sar-framework

[![GitHub release](https://img.shields.io/badge/Release-v0.3.0-brightgreen.svg)](https://github.com/StephanPirnbaum/sar-framework/releases/latest)

The SAR-Framework is a tool supporting the process of understanding and refactoring aged, unstructured Java software systems. It therefore analyses the cohesion and coupling properties of software systems scanned with jQAssistant and results in an optimal hierarchical structure for the software system. 

## Usage

### Scan application using jQAssistant

First off, it is necessary to scan your application using jQAssistant in order to obtain a Neo4j database containing the structure of the software system. This can be accomplished in two ways. First, use the jQAssistant command line scanner provided under [1] or using the `jqassistant-maven-plugin`. Both usages are well-described in the Getting Started guide under [2]. Once done, you will find a `store` directory which contains the Neo4j database.

### Import Neo4j store into SAR-Framework
After scanning your application, download the SAR-Framework from [3]. It is best to start the SAR-Framework from command line to get updates on the current execution state. After launching the SAR-Framework, choose the store directory and connect to the database. This may take a short while.   

### Configure and run SAR-Framework
Below the database section, you will face the configuration settings of the SAR-Framework. It is important to specify what classes shall be considered during the analysis and structuring process to obtain unbiased results and to reduce the execution time. Let's consider we want to analyse the dukecon-server application, which can be found under [4].

The `Artifact` setting reduces the analyzed classes to those being part of the specified artifact. We will leave it as is, meaning the regular expression `.*`, leading to considering all artifacts.

The `Base Package` setting reduces the analyzed classes to those residing inside the specified package. We will use `org\.dukecon.*` to select only those classes that are actually part of the dukecon-server application.

The `Type Name` setting reduces the analyzed classes to those whose name matches the specification. Since the dukecon-server artifact will contain generated classes and thus such classes will not help during the application restructuring process, we want to exclude those classes. Generated classes contain `$_`, so following regular expression will exclude them: `^((?!\\$_).)*`

Note: The three settings work in conjunction to each other meaning that all three settings have to match.

The last two settings configure the underlying genetic algorithms and will affect the result quality and runtime. The `Generations` setting affects, frankly spoken, the depth while the `Population Size`setting influences the broadth of the search. For detailed information it is recommeded to read up on genetic algorithms under [5]. With the standard setting, an application with around 300 classes will take around 10 minutes to be analyzed and structured. For large(r) software systems, decreasing the population size is recommended. 

Note: The next release will bring some improvements for the execution time.

### View and interpret results

Once finished, the SAR_Framework generates a .zip-archive named Result_<timestamp>.zip. It contains the result of the analysis in both textual and interactive graphical representations. 


[1] https://jqassistant.org/wp-content/uploads/2017/06/jqassistant-commandline-1.3.0.zip

[2] https://jqassistant.org/get-started/

[3] https://github.com/StephanPirnbaum/sar-framework/releases/latest

[4] https://github.com/dukecon/dukecon_server

[5] http://natureofcode.com/book/chapter-9-the-evolution-of-code/
