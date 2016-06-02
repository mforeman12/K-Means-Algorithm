//Program:     Foreman4
//Course:      Current Topics
//Description: Implements K means algorithm to sort data points into
//             k clusters, as given by the user.
//Author:      Matt Foreman
//Revised:     10/28/15
//Language:    Java
//IDE:         NetBeans 8.0.1
//****************************************************************************
//****************************************************************************
package Foreman4;

import java.util.concurrent.ThreadLocalRandom;
import java.util.ArrayList;

//Class:       Foreman4
//Description: Runs the program by creating multiple instances of the cluster
//             class, and performing the K means algorithm on them.

public class Foreman4 {

ArrayList<Cluster> clusters;
ArrayList<double[]> DataPoints;
ArrayList<Integer> indexes;
double[] minimums;
double[] maximums;
int k;
static boolean progRun = true;

//****************************************************************************
//****************************************************************************

//Method:      Foreman4
//Description: Constructor called for every instance of the program in order to
//             initialize and reset values for each run.
//Parameters:  none
//Returns:     nothing
//Calls:       nothing
public Foreman4() {
clusters = new ArrayList<>();
DataPoints = new ArrayList<>();
indexes = new ArrayList<>();
k = 0;
progRun = true;

}

//****************************************************************************

//Method:      main
//Description: Runs the program. Reads in user input, and passes it to helper
//             methods, which instantiate instances of the Cluster class.
//Parameters:  none
//Returns:     nothing
//Calls:       KeyboardInputClass
//             TextFileClass
//             Foreman4
//             setMaxMin
//             normalizeValues
//             selectSeeds
//             assignDataPointsToClusters
//             padString
public static void main(String[] args) {
while(progRun == true){
KeyboardInputClass keyboardInput = new KeyboardInputClass();
TextFileClass text = new TextFileClass();

text.getFileName("Specify the text file to be read:\n");
text.getFileContents();

int normalize = keyboardInput.getInteger(true, 2, 1, 2,
        "\nNormalize values? (default: no) \n1. yes \n2. no\n");

Foreman4 foreman = new Foreman4();

for (int i = 0; i < text.lineCount; i++) {
    String[] inputString = text.text[i].replaceAll(
            "\\ ", "").replaceAll("\\{", "").replaceAll(
                    "\\}", "").split(",");
    if (!"".equals(inputString[0])) {
        double[] dataPoint = new double[inputString.length - 1];
        for (int j = 0; j < dataPoint.length + 1; j++) {
            if(j == 0){
               foreman.indexes.add(Integer.parseInt(inputString[j]));
            }
            else{
            dataPoint[j - 1] = Integer.parseInt(inputString[j]);
            }
        }

        foreman.DataPoints.add(dataPoint);

    }

}
foreman.setMaxMin();
if (normalize == 1) {
    foreman.normalizeValues();
}

foreman.k = keyboardInput.getInteger(true, 3, 1,
foreman.DataPoints.size(), "\nEnter desired K value (default: 3)\n");

foreman.selectSeeds();
foreman.assignDataPointsToClusters();


boolean run = true;
while(run == true){
    boolean converged = true;
    for (int j = 0; j < foreman.clusters.size(); j++) {
        foreman.clusters.get(j).compareClustertoPreviousCluster();
        if(foreman.clusters.get(j).converged == false){
            converged = false;
        }

    }
    if(converged == false){
    foreman.kMeans();
    }
    else{
       run = false;
    }
}

for (int i = 0; i < foreman.clusters.size(); i++) {
    System.out.println("\nCluster " + i + ":");

        System.out.print(padString("ID", true, 10, " "));
        System.out.print(padString("Height", true, 10, " "));
        System.out.print(padString("Weight", true, 10, " "));
        System.out.print(padString("Sex", true, 10, " "));
        System.out.print(padString("College Education", true, 10, " "));
        System.out.print(padString("Athleticism", true, 10, " "));
        System.out.print(padString("RAD Rating", true, 10, " "));
        System.out.print(padString("Age", true, 10, " "));
        System.out.print(padString("Income", true, 10, " "));
        System.out.println("");
        int[] wordLength = new int[9];
        wordLength[0] = 2; wordLength[1] = 6; wordLength[2] = 6;
        wordLength[3] = 3; wordLength[4] = 17; wordLength[5] = 11;
        wordLength[6] = 10; wordLength[7] = 3; wordLength[8] = 6;

    for (int j = 0; j < foreman.clusters.get(i).DataPoints.size(); j++){

        System.out.print(padString("", true, 10, " "));
        System.out.print(padString(
                foreman.clusters.get(i).indexes.get(j) + "", false,
                12 - foreman.clusters.get(i).indexes.get(j).
                        toString().length(), " "));

for (int l = 0; l < foreman.clusters.get(i).DataPoints.get(j).length; l++) {
    if(normalize == 2){
        String num =(int)foreman.clusters.get(i).DataPoints.get(j)[l] + "";
        System.out.print(padString(
                num, false, 10 + wordLength[l + 1] - num.length(), " "));

    }
   else{
       String num = (int)(foreman.clusters.get(i).DataPoints.get(j)[l]
              * (foreman.maximums[l] - foreman.minimums[l])
               + foreman.minimums[l]) + "";
        System.out.print(padString(
                num, false, 10 + wordLength[l + 1] - num.length(), " "));

            }
        }
        System.out.println("");

    }
    System.out.println("");
}

if(keyboardInput.getInteger(true, 2, 1, 2, "\nWould you like to enter "
        + "another file? (default: no) \n1. yes \n2. no\n") == 2)
{progRun = false;}

}//end of while loop
}//end of main method

//****************************************************************************

//Method:      selectSeeds
//Description: Randomly selects k elements from DataPoints to be seeds for the
//             initial pass through the K means algorithm
//Parameters:  none
//Returns:     nothing
//Calls:       Cluster constructor method

public void selectSeeds() {
ArrayList<Integer> seed = new ArrayList<>();
for (int i = 0; i < k; i++) {
    boolean uniqueSeed = false;
    while (!uniqueSeed) {
        int seedIndex = 
                ThreadLocalRandom.current().nextInt(0, DataPoints.size());
        if (seed.indexOf(seedIndex) == -1) {
            seed.add(seedIndex);
            uniqueSeed = true;
        }
    }

    Cluster clust = new Cluster(DataPoints.get(seed.get(i)));
    clusters.add(clust);
}

}

//****************************************************************************

//Method:      computeBestCentroid
//Description: Matches each seed (or centroid) to its corresponding DataPoints
//             by comparing each DataPoint's distance from each seed
//             (or centroid)
//Parameters:  dataPoint
//Returns:     int
//Calls:       nothing

public int computeBestCentroid(double[] dataPoint) {
double bestDistance = Double.MAX_VALUE;
int bestCentroid = 0;

for (int i = 0; i < clusters.size(); i++) {
    double x = 0;
    for (int j = 0; j < dataPoint.length; j++) {
        x += Math.pow(dataPoint[j] - clusters.get(i).centroid[j], 2);

    }
    if (Math.sqrt(x) < bestDistance) {
        bestDistance = Math.sqrt(x);
        bestCentroid = i;
    }

}

return bestCentroid;
}

//****************************************************************************

//Method:      assignDataPointsToClusters
//Description: Assigns each DataPoint to its corresponding cluster, based on
//             information returned from computeBestCentroid
//Parameters:  none
//Returns:     nothing
//Calls:       nothing

public void assignDataPointsToClusters() {
for (int i = 0; i < DataPoints.size(); i++) {
    int clust = computeBestCentroid(DataPoints.get(i));
    clusters.get(clust).DataPoints.add(
                    DataPoints.get(i));
    clusters.get(clust).indexes.add(
                    indexes.get(i));

}
}

//****************************************************************************

//Method:      setMaxMin
//Description: Finds the maximum and minimum values for each element of a
//             DataPoint. We use this information to normalize inputs and
//             to create a range within which a random seed can be selected
//             for the initial run of the K means algorithm.
//Parameters:  none
//Returns:     nothing
//Calls:       nothing

public void setMaxMin() {
minimums = new double[DataPoints.get(0).length];
maximums = new double[DataPoints.get(0).length];

for (int i = 0; i < minimums.length; i++) {
    minimums[i] = Double.MAX_VALUE;
    maximums[i] = Double.MIN_VALUE;
}

for (int i = 0; i < DataPoints.size(); i++) {
    for (int j = 0; j < DataPoints.get(0).length; j++) {
        if (DataPoints.get(i)[j] < minimums[j]) {
            minimums[j] = DataPoints.get(i)[j];
        }
        if (DataPoints.get(i)[j] > maximums[j]) {
            maximums[j] = DataPoints.get(i)[j];
        }

    }

}
}

//****************************************************************************

//Method:      normalizeValues
//Description: Normalizes the values of each element in a given DataPoint,
//             such that we can have a more accurate clustering.
//Parameters:  none
//Returns:     nothing
//Calls:       nothing

public void normalizeValues() {

for (int i = 0; i < DataPoints.size(); i++) {
    for (int j = 0; j < DataPoints.get(0).length; j++) {

            DataPoints.get(i)[j] = (DataPoints.get(i)[j] - minimums[j])
                    / (maximums[j] - minimums[j]);

    }

}

}

//****************************************************************************

//Method:      kMeans
//Description: Runs the K means algorithm by first creating new centroids for
//             each cluster, then reassigning each DataPoint to a new cluster.
//             DataPoints and indexes are both cleared each time this method is
//             called, so that each cluster can be reformed from the new
//             centroid.
//Parameters:  none
//Returns:     nothing
//Calls:       newCentroid
//             assignDataPointsToClusters

public void kMeans(){
for (int i = 0; i < clusters.size(); i++) {
    clusters.get(i).newCentroid();
    clusters.get(i).DataPoints.clear();
    clusters.get(i).indexes.clear();
}

assignDataPointsToClusters();
}

//****************************************************************************
//Method:      padString
//Description: Pads a string with a specified number of leading or trailing
//             characters. The character (or block of characters) to be used
//             for padding is user definable. Example: if stringToBePadded =
//             "test" and padLeft = true and numberOfSpacesToPad = 4 and
//             paddingCharacters = " " (i.e., a single blank space), the
//             returned string would be " test" (i.e., the input string
//             preceded by 4 blank spaces). However, if paddingCharacters =
//             "123" the returned string would be "123123123123test". Example
//             use: variableAsString = Integer.toString(variable);
//             variableAsString = padString(variableAsString, true,
//             totalSizeOfPrintField - variableAsString.length(), " ");
//             System.out.print(variableAsString + " "); Parameters:
//             stringToBePadded - the string to be modified padLeft -
//             true=place padding characters on left of the string to be padded;
//             false=pad right numberOfSpacesToPad - the # of leading or
//             trailing positions to be padded paddingCharacters - the
//             character(s) to be used for padding Returns: paddedString -
//             the modified string
//Calls:       nothing

public static String padString(
        String stringToBePadded, boolean padLeft, 
        int numberOfSpacesToPad, String paddingCharacters) {
    
String paddedString = stringToBePadded;
if (padLeft)
for (int i = 1; i <= numberOfSpacesToPad; i++)
paddedString = paddingCharacters + paddedString;
else
for (int i = 1; i <= numberOfSpacesToPad; i++)
paddedString = paddedString + paddingCharacters;
return paddedString;
}
//****************************************************************************

}

//Class:       Cluster
//Description: Contains all cluster data, including centroids, DataPoints
//             contained by that cluster, and DataPoints from the cluster 
//             in the previous run of K means

class Cluster {

public ArrayList<double[]> DataPoints;
public ArrayList<Integer> indexes;
public ArrayList<double[]> PreviousDataPoints;
public double[] centroid;
public boolean converged = true;

//****************************************************************************
//****************************************************************************

//Method:      Cluster
//Description: Constructor called for every instance of a Cluster.
//Parameters:  seed - used to create the cluster for the inital run of K means
//Returns:     nothing
//Calls:       nothing

public Cluster(double[] seed) {
    centroid = new double[seed.length];
    System.arraycopy(seed, 0, centroid, 0, seed.length);
    DataPoints = new ArrayList<>();
    indexes = new ArrayList<>();
    PreviousDataPoints = new ArrayList<>();
}

//****************************************************************************

//Method:      newCentroid
//Description: Recomputes the value of the centroid for a cluster after each
//             run of K means
//Parameters:  none
//Returns:     nothing
//Calls:       copyArrayList

public void newCentroid() {
    copyArrayList();

    double[] newCentroid = new double[centroid.length];

    for (int i = 0; i < DataPoints.get(0).length; i++) {
        double tally = 0;
        for (int j = 0; j < DataPoints.size(); j++) {
            tally += DataPoints.get(j)[i];

        }
        newCentroid[i] = tally / DataPoints.size();
    }
    for (int i = 0; i < newCentroid.length; i++) {
        centroid[i] = newCentroid[i];

    }
}

//****************************************************************************

//Method:      copyArrayList
//Description: Keeps track of the DataPoints in a cluster after the cluster is
//             cleared in order to compare those points to the new points
//             assigned to that cluster for the next run of K means. This is
//             done in order to check whether the algorithm has converged.
//Parameters:  none
//Returns:     nothing
//Calls:       nothing

public void copyArrayList() {
    for (int i = 0; i < DataPoints.size(); i++) {
        PreviousDataPoints.add(DataPoints.get(i));

    }

}

//****************************************************************************

//Method:      compareClustertoPreviousCluster
//Description: Compares PreviousDataPoints to DataPoints, as explained in
//             copyArrayList
//Parameters:  none
//Returns:     nothing
//Calls:       nothing

public void compareClustertoPreviousCluster() {
    if (!PreviousDataPoints.isEmpty()) {
        for (int i = 0; i < DataPoints.size(); i++) {
            if (DataPoints.get(i) != PreviousDataPoints.get(i)) {
                converged = false;
            }
        }
    }
}

//****************************************************************************

}
