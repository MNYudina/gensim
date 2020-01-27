## 1. Purpose
An project allow to generate random graphs and analyze network processes.

## 2. Sources
To use the code, you should download the repository and install the libraries based on the provided pom.xml file. The libraries used are common-collection, colt, jung, junit, common-cli. The program is written in java 8, respectively, you must use> = jdk 8

## 3. Launch
If the JRE is installed, then the program can be launched using the command line:

* java -jar target/gensim-0.0.1.jar

### The following parameters are taken into account

#### -g
It indicates used network graphs. Three different values ​​are possible
 * as - graph of the network of autonomous systems (M. Newman, 2003)
 * ba -  Barabashi-Albert graph
 * npa - graph with non-linear preferential attachment rule
 
#### -d
It indicates the need to display the distribution of the degree of connectivity of the vertices of the graph. A parameter is any positive integer value, for example 10

#### -l
It indicates the need to display the distribution of the degree of connectivity of the end degrees of connectivity of the edges of the graph, this is a two-dimensional distribution, the parameter is any positive integer value k, in this case all the values ​​Q_ {1, ..., k; 1 will be entered in the file e_out.txt , ..., k}
 
#### -n
It is used if it is calculating the fraction of vertices in the maximum connected component if  given fraction p of vertices are removed, p = 0.1,0.2, ..., 0.9
 
#### -e
It is used if it is calculating fraction of vertices in the maximum connected component if given fraction p of the edges are removed, p = 0.1,0.2, ..., 0.9
 
#### -v 
It is used if it is calculating fraction of infected nodes if  probability of virus spread \ lyambda = 0.1,0.2, ..., 0.9
 
## 4. Examples of use

Examples are presented in run.bat file