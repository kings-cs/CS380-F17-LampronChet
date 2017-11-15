

# CS380-F17-LampronChet

Hello I'm PIP! It's nice to meet you!
I will handle all of your image processing needs.


Table of Contents:
I: Basic Instructions
II: Image editing
III: Closing the program
IV: Known Bugs
V: Timing chart
VI: Supported File types

********************************************************

I. Basic instructions:
	<br>	1. To run the file run the PipGui.java out of the source folder.
	<br>	2. Open a file using the Open menu item or ctrl-o.
	<br>	3. Save a file using the Save menu item or ctrl-s.
 	<br>	//When saving the image, if the filetype is not specified in the filename it will default to the type of png. THIS FEATURE DOES NOT WORK YET PLEASE SEE BUGS.
	<br> 	4. Open the raw README.md by clicking about or ctrl-a.
	<br>	5. Close a file using the Close menu item, but be careful I treat this as my garbage collector, the FILE WILL BE LOST!
	<br>    6. Mosaic asks for a number of tiles. Please enter an integer value like 2048. A custom JOptionPane is used here so that you may cancel your tiles selection without running Mosaic.
	
II. Image editing:
	<br>Implemented items:
	<br>1. Grayscale image
	<br>2. Grayscale in parallel
	<br>3. Sepia
	<br>4. Sepia in parallel
	<br>5. Blur
	<br>6. Blur in parallel
	<br>7. Rotate left in parallel
	<br>8. Rotate right in parallel
	<br>9. Vertical flip in parallel
	<br>10. Horizontal flip in parallel
	<br>11. Mosaic
	<br>12. Mosaic in parallel
	<br>13. Grayscale Histogram Equalization
	<br>14. Efficient Histogram Equalization
	<br>15. Zoom in and out in increments of 10%
	<br>Many more to come soon!

III. Close the program:
	<br>1.You can close the program with the normal x, the close menu option, or alt-f4. 
	
IV. Bugs:
	<br>About is implemented, but only displays the raw text currently. Raw text link must be updated often, when not updated the program will not run. This feature has been removed until a permanent solution is found.
	<br>.gif will only display the first frame in the file.
	<br>Saving without a file type does not make PNG, please specify the file type in the name.
	
V. Runtime chart:

<table style = "width:50%">
  <tr>
    <th> Algorithm </th>
    <th> Time </th>
    <th> Device </th>
    <th> Operating System </th>
  </tr>
  <tr>
    <td> Grayscale </td>
    <td> 35ms </td>
    <td> Nvidia GTX 745 </td>
    <td> Ubuntu </td>
  </tr>
  
  <tr>
    <td> Grayscale Parallel </td>
    <td> 2.79ms </td>
    <td> Nvidia GTX 745 </td>
    <td> Ubuntu </td>
  </tr>
  
  
  <tr>
    <td> Sepia </td>
    <td> 39ms </td>
    <td> Nvidia GTX 745 </td>
    <td> Ubuntu </td>
  </tr>
  
  
  <tr>
    <td> Sepia Parallel</td>
    <td> 2.82ms </td>
    <td> Nvidia GTX 745 </td>
    <td> Ubuntu </td>
  </tr>
  
  <tr>
    <td> Blur </td>
    <td> 129ms </td>
    <td> Nvidia GTX 745 </td>
    <td> Ubuntu </td>
  </tr>
  
  
  <tr>
    <td> Blur Parallel</td>
    <td> 5ms </td>
    <td> Nvidia GTX 745 </td>
    <td> Ubuntu </td>
  </tr>
  
  <tr>
    <td> Rotate left</td>
    <td> 2.27ms </td>
    <td> Nvidia GTX 745 </td>
    <td> Ubuntu </td>
  </tr>
  
  <tr>
    <td> Rotate right</td>
    <td> 0.82ms </td>
    <td> Nvidia GTX 745 </td>
    <td> Ubuntu </td>
  </tr>
  
  <tr>
    <td> Vertical flip</td>
    <td> 0.81ms </td>
    <td> Nvidia GTX 745 </td>
    <td> Ubuntu </td>
  </tr>
  
  <tr>
    <td> Horizontal flip</td>
    <td> 2.34ms </td>
    <td> Nvidia GTX 745 </td>
    <td> Ubuntu </td>
  </tr>
  
  <tr>
    <td> Mosaic</td>
    <td> 1382.61ms </td>
    <td> Nvidia GTX 745 </td>
    <td> Ubuntu </td>
  </tr>
  
  <tr>
    <td> Mosaic Parallel</td>
    <td> 4.27ms </td>
    <td> Nvidia GTX 745 </td>
    <td> Ubuntu </td>
  </tr>
  
<tr>
    <td> Grayscale </td>
    <td> 54ms </td>
    <td> Nvidia GTX 970 </td>
    <td> Windows 10 </td>
  </tr>
  
  <tr>
    <td> Grayscale Parallel </td>
    <td> 2ms </td>
    <td> Nvidia GTX 970 </td>
    <td> Windows 10 </td>
  </tr>
  
  
  <tr>
    <td> Sepia </td>
    <td> 62ms </td>
    <td> Nvidia GTX 970 </td>
    <td> Windows 10 </td>
  </tr>
  
  
  <tr>
    <td> Sepia Parallel</td>
    <td> 2ms </td>
    <td> Nvidia GTX 970 </td>
    <td> Windows 10 </td>
  </tr>
  
  <tr>
    <td> Blur </td>
    <td> 165ms </td>
    <td> Nvidia GTX 970 </td>
    <td> Windows 10 </td>
  </tr>
  
  
  <tr>
    <td> Blur Parallel</td>
    <td> 21ms </td>
    <td> Nvidia GTX 970 </td>
    <td> Windows 10 </td>
  </tr>
  
  <tr>
    <td> Grayscale Equalization</td>
    <td> 35.8ms </td>
    <td> Nvidia GTX 970 </td>
    <td> Windows 10 </td>
  </tr>
  
  <tr>
    <td> Grayscale Equalization Optimized</td>
    <td> 31.4ms </td>
    <td> Nvidia GTX 970 </td>
    <td> Windows 10 </td>
  </tr>
  
  <tr>
    <td> Radix sort on a size of 250</td>
    <td> 46.4ms </td>
    <td> Nvidia GTX 970 </td>
    <td> Windows 10 </td>
  </tr>
  
   <tr>
    <td> Radix sort on a size of 4096</td>
    <td> 55.2ms </td>
    <td> Nvidia GTX 970 </td>
    <td> Windows 10 </td>
  </tr>
  
   <tr>
    <td> Radix sort on a size of 1048576</td>
    <td> 2312.3ms </td>
    <td> Nvidia GTX 970 </td>
    <td> Windows 10 </td>
  </tr>

</table>

VI. Supported File Types:
<br>.png
<br>.jpg
<br>.jpeg
<br>.gif

VII. Histogram Optimizations:
<br>To optimize this algorithm I have moved the image data into local memory. This did not reduce the atomics, but did reduce the runtime. 
<br>Each algorithm was run 10 times on an image of size 1920 x 1200. The optimized algorithm ran 4ms faster on average.

VIII. Radix notes:
<br>Radix has been implemented. The test classes can be found in src/testing/RadixTest. The code can be found in src/pinklprocessing/Radix.
<br> I attempted to convert pad array to parallel, but that left me with a division by 0 in my kernel and a log file with a stack trace that I could not read. The code is there, but commented out.
<br> I have coded all pieces of the radix sort in parallel, the sequential code is left in place but commented out.
<br> The kernels can be found in /Kernels/RadixKernels.
