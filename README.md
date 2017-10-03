
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
	
II. Image editing:
	<br>Implemented items:
	<br>1. Grayscale image
	<br>2. Grayscale in parallel
	<br>3. Sepia
	<br>4. Sepia in parallel
	<br>5. Blur
	<br>6. Blur in parallel
	<br>Many more to come soon!

III. Close the program:
	<br>1.You can close the program with the normal x, the close menu option, or alt-f4. 
	
IV. Bugs:
	<br>About is implemented, but only displays the raw text currently. Raw text link must be updated semi frequently.
	<br>.gif will only display the first image in the file.
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
    <td> <1ms </td>
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
    <td> < 1ms </td>
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

</table>

VI. Supported File Types:
<br>.png
<br>.jpg
<br>.jpeg
<br>.gif
