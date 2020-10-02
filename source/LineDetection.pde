import processing.video.*;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.Collections;

float discretizationStepsPhi = 0.06f; 
float discretizationStepsR = 2.8f;
// dimensions of the accumulator
int phiDim = (int) (Math.PI / discretizationStepsPhi +1);
//The max radius is the image diagonal, but it can be also negative


List<PVector> hough(PImage edgeImg, int n) throws Exception {


  //int minVotes = Math.min(edgeImg.width/4, edgeImg.height/4);
  //int regionSize = Math.min(edgeImg.width/30, edgeImg.height/30);
  int minVotes = 20;
  int regionSize = 1;


  int rDim = (int) ((sqrt(edgeImg.width*edgeImg.width +
    edgeImg.height*edgeImg.height) * 2) / discretizationStepsR +1);

  // our accumulator
  int[] accumulator = new int[phiDim * rDim];


  // Fill the accumulator: on edge points (ie, white pixels of the edge
  // image), store all possible (r, phi) pairs describing lines going
  // through the point.
  for (int y = 0; y < edgeImg.height; y++) {
    for (int x = 0; x < edgeImg.width; x++) {
      // Are we on an edge?
      if (brightness(edgeImg.pixels[y * edgeImg.width + x]) != 0) {
        // ...determine here all the lines (r, phi) passing through
        // pixel (x,y), convert (r,phi) to coordinates in the
        // accumulator, and increment accordingly the accumulator.
        // Be careful: r may be negative, so you may want to center onto
        // the accumulator: r += rDim / 2

        for (int phiStep = 0; phiStep < phiDim; phiStep += 1) {

          double phi = phiStep * discretizationStepsPhi ;
          if (phi < 0 || phi > Math.PI) throw new Exception("phi : "+phi+" not part of [0,PI]");

          //double r = x * Math.cos(phi) + y * Math.sin(phi) ;
          //r *= 1./discretizationStepsR;
          double r = x * tabCos[phiStep] + y * tabSin[phiStep] ;
          r += rDim / 2.; // center r
          if (r >= rDim || r < 0 ) throw new Exception("r : "+r+" not part of [0,"+rDim+"]");

          accumulator[(int)(phiStep * rDim + r)] += 1.;
        }
      }
    }
  }


  // ------- DEBUG --> DISPLAY ACCUMULATOR

  //PImage houghImg = createImage(rDim, phiDim, ALPHA);
  //for (int i = 0; i < accumulator.length; i++) {
  //  houghImg.pixels[i] = color(min(255, accumulator[i]));
  //}
  //// You may want to resize the accumulator to make it easier to see:
  //houghImg.resize(400, 400);
  //houghImg.updatePixels();
  //image(houghImg , edgeImg.width , 0 );

  // -------


  ArrayList<PVector> lines=new ArrayList<PVector>();
  List<Integer> bestCandidates = new ArrayList<Integer>();

  for (int idx = 0; idx < accumulator.length; idx++) {
    if (accumulator[idx] > minVotes ) {
      bestCandidates.add(idx);
    }
  }
  //println("bestCandidates.size() - pre = " + bestCandidates.size());
  Collections.sort(bestCandidates, new HoughComparator(accumulator));
  if (bestCandidates.size()>n)
    bestCandidates = bestCandidates.subList(0, n);
  //println("bestCandidates.size() - post = " + bestCandidates.size());





  for (int idx : bestCandidates) {
    // Computes back the (r, phi) polar coordinates:
    int accPhi = (int) (idx / (rDim));
    int accR = idx - (accPhi) * (rDim);
    float r = (accR - (rDim) * 0.5f) * discretizationStepsR;
    float phi = accPhi * discretizationStepsPhi;
    if (checkMaxNeighbor(accumulator, idx, regionSize, phiDim)) {
      lines.add(new PVector(r, phi));
    }
  }
  //println("lines size = " + lines.size());

  return lines;
}

boolean checkMaxNeighbor(int[] accumulator, int idx, int regionSize, int phiDim) {
  int potentialMax = accumulator[idx];
  for (int i = -regionSize; i<=regionSize; ++i) {
    for (int j = -regionSize; j<=regionSize; ++j) {
      if ((idx +i + j*phiDim)>=0 && (idx +i + j*phiDim) < accumulator.length && accumulator[idx+i+j*phiDim ] > potentialMax)
        return false;
    }
  }
  return true;
}


class HoughComparator implements java.util.Comparator<Integer> { 
  int[] accumulator;

  public HoughComparator(int[] accumulator) {
    this.accumulator = accumulator;
  }

  @Override
    public int compare(Integer l1, Integer l2) { 
    if (accumulator[l1] > accumulator[l2] || (accumulator[l1] == accumulator[l2] && l1 < l2)) 
      return -1; 
    return 1;
  }
}
