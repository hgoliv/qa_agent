package pt.uc.dei.qa.filters;

public class AccentStripper {

	public int strip(char s[], int len) {
		
	    for (int i = 0; i < len; i++)
	      switch(s[i]) {
	        case 'à': 
	        case 'á':
	        case 'â':
	        case 'ä': 
	        case 'ã': s[i] = 'a'; break;
	        case 'ò':
	        case 'ó':
	        case 'ô':
	        case 'ö': 
	        case 'õ': s[i] = 'o'; break;
	        case 'è':
	        case 'é':
	        case 'ê':
	        case 'ë': s[i] = 'e'; break;
	        case 'ù':
	        case 'ú':
	        case 'û':
	        case 'ü': s[i] = 'u'; break;
	        case 'ì':
	        case 'í':
	        case 'î':
	        case 'ï': s[i] = 'i'; break;
	        case 'ç': s[i] = 'c'; break;
	      }

	    return len;
	  }
}
