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
	<br>	2. Open a file using the Open menu item.
	<br>	3. Save a file using the Save menu item or Alt-s. After the file menu is open.
 	 <br>	//When saving the image, if the filetype is not specified in the filename it will default to the type of png.
	<br>	4. Close a file using the Close menu item, but be careful I treat this as my garbage collector, the FILE WILL BE LOST!
	
II. Image editing:
	<br>Implemented items:
	<br>1. Grayscale image
	<br>2. Grayscale in parallel
	<br>3. Sepia
	<br>4. Sepia in parallel
	<br>Many more to come soon!

III. Close the program:
	<br>1. Please use the x in the top right of your window to close the program properly. 
	
IV. Bugs:
	<br>About is not implemented yet.
	<br>.gif will only display the first image in the file.
	<br>Key mnemonic for save only works when the file menu is open.
	
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

</table>

VI. Supported File Types:
<br>.png
<br>.jpg
<br>.jpeg
<br>.gif
