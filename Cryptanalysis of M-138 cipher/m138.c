/*
 * m138.c
 *
 *      Created on: Mar 10, 2016
 *      Author: Ketki Kulkarni & Pratikshya Mishra
 */
#include <stdio.h>
#include <string.h>
#include <stdbool.h>
struct stripset
{
	/*
	 * 		Each object of struct contains strip number and strip characters containing 26 alphabets in random order
	 */
    int num;
    unsigned  char stripChar[26];
};

void addOffset(int (*possibleOffset)[100], int elementNo, int *offsetCount, int offset);

int main()
{
	/*
	 * 			Variable declaration
	 */
    FILE *file;
    file = fopen("stripset.txt", "rt");

    int temp=0,previous=0;
    int i = 0, j = 0, k = 0, l=0;
    int offset = 0, offset_blk1 = 0,offset_blk2 = 0;
    int stripSet[25];
    int possibleOffset[100][100];
    int pos_cipher_blk1=0,pos_plain_blk1=0,pos_cipher_blk2=0,pos_plain_blk2=0;
    int offsetCount[100],tmpOffset[100];

    /*
     * 			Cipher text of 100 charcters
     */
    char ciphertext[] =   "BBQGFHSDXNFKLXRREYYPADREWTFRJGJDCBGDZFXINXMWYLHTGPAXHOLTHXPRCTTADFWOJYXAEYRNKRXRXDKHSFDUVPXQGWMKMYKZ";

    /*
     * 			First 48 characters of plain text are given
     */
    char tmpPlaintext[] = "TWOTHINGSAREINFINITETHEUNIVERSEANDHUMANSTUPIDITY";
    char plaintext[100];

    /*
     * 			an array of 100 strips
     */
    struct stripset strips[100];

    for(i=0; i<100; i++)
        for(j=0; j<100; j++)
            possibleOffset[i][j] = 0;

    char buff[32];

    /*
     * 			Reading strips from text file
     */
    i = 0;
    while(fgets(buff, 32, (FILE *)file)!=NULL)
    {
        int num = (int)((buff[0] - '0')*10) + (buff[1] - '0');
        strips[i].num = num;

        k = 0; j=3;
        while(k<26)
        {
            char tmp = buff[j];
            strips[i].stripChar[k] = tmp;
            k++;
            j++;
        }
        i++;
    }
    fclose(file);

    /*
     * 			Initializing the matrix of possible offsets
     */
    for(i=0;i<100;i++)
    {
    	offsetCount[i]=0;
    	tmpOffset[i]=0;
    }

    /*
     * 			For each given strip finding the position of cipher text character and plain text character of first 24 characters of each block
     * 			Here cipher text is 100 characters and there are 4 blocks of cipher text each containing 25 characters.
     */
    for(i=0;i<100;i++)
    {
    	for(j=0;j<23;j++)
    	{
    		/*
    		 * 			Finding the position of cipher text character in block 1
    		 */
    		for (k = 0; k < 26; k++)
    		{
    			if(strips[i].stripChar[k] == ciphertext[j])
				{
					pos_cipher_blk1 = k;
					break;
				}
			}

    		/*
			 * 			Finding the position of plain text character in block 1
			 */
    		for (k = 0; k < 26; k++)
    		{
    			if(strips[i].stripChar[k] == tmpPlaintext[j])
				{
					pos_plain_blk1 = k;
					break;
				}
    		}

    		/*
    		 * 			Calculation offset for block 1
    		 */
			offset_blk1 = pos_cipher_blk1 - pos_plain_blk1;
			if(offset_blk1<0)
				offset_blk1 = offset_blk1 + 26;

			/*
			 * 			Finding the position of cipher text character in block 2
			 */
    		for (k = 0; k < 26; k++)
    		{
    			if(strips[i].stripChar[k] == ciphertext[j+25])
				{
					//printf("%c == %c\n", strips[j].stripChar[k],p1);
					pos_cipher_blk2 = k;
					break;
				}
			}

    		/*
			 * 			Finding the position of plain text character in block 2
			 */
    		for (k = 0; k < 26; k++)
    		{
    			if(strips[i].stripChar[k] == tmpPlaintext[j+25])
				{
					//printf("%c == %c\n", strips[j].stripChar[k],c1);
					pos_plain_blk2 = k;
					break;
				}
    		}

    		/*
			 * 			Calculation offset for block 2
			 */
    		offset_blk2 = pos_cipher_blk2 - pos_plain_blk2;
    		if(offset_blk2<0)
				offset_blk2 = offset_blk2 + 26;

    		/*
    		 * 			If offset of block 1 and block 2 are equal then considering that offset and adding it to matrix of possible offsets
    		 */
            if(offset_blk2==offset_blk1)
            {
            	addOffset(possibleOffset,j,&offsetCount[j],offset_blk1);
            	//printf("%d\t",possibleOffset[i][j]);
            }
    	}
    }

    /*
     * 			Finding the offset in the matrix such that offset is present for all the characters of block 1 i.e. offset occurs 25 times in the matrix
     */
	for (j = 0; j < 23; j++)
	{
		for( k=0; k<offsetCount[j]; k++) {
			temp = possibleOffset[j][k];
			//printf("temp= %d offsetCount=%d \n", temp,offsetCount[j]);
			previous = 0;
			for(i=0; i<k; i++) {
				if (temp == possibleOffset[j][i] ) {
					previous = 1;
					break;
				}
			}
			if (previous== 0) {
				tmpOffset[temp]++;
			}
		}
	}

	/*	for (i = 0; i < 100; i++) {
		printf(" %d \n", tmpOffset[i]);
	}*/

	for (i = 0; i < 100; i++) {
		if (tmpOffset[i] == 23) {
			offset = i;
		}
	}
	//printf("offset = %d \n", offset);

	/*
	 * 			Initializing the matrix so as to compute possible strips that can be used to decipher
	 */
	for(i=0; i<100; i++)
	{
		for(j=0; j<25; j++)
		{
			possibleOffset[i][j] = 111;
		}
	}

	for(i=0;i<100;i++)
	{
		offsetCount[i]=0;
	}

	/*
	 * 			Finding all strips for each character in block 1 and block 2 such that
	 * 			position of cipher text character minus offset gives position of respective plain text character
	 */
	for(i=0; i<23; i++)
	{
		l = 0;
		for(j=0; j<100; j++)
		{
			/*
			 * 			Finding position of each cipher text character of block 1
			 */
			for(k=0; k<26; k++)
			{
    			if(strips[j].stripChar[k] == ciphertext[i])
				{
					//printf("%c == %c\n", strips[j].stripChar[k],p1);
					pos_cipher_blk1 = k;
					break;
				}
			}

			/*
			 * 			Position of cipher text character of block 1 - offset = position of plain text character of block 1
			 */
			pos_plain_blk1 = pos_cipher_blk1 - offset;
			if(pos_plain_blk1<0)
				pos_plain_blk1 = pos_plain_blk1 + 26;


			/*
			 * 			Finding position of each cipher text character of block 1
			 */
			for(k=0; k<26; k++)
			{
				if(strips[j].stripChar[k] == ciphertext[i+25])
				{
					//printf("%c == %c\n", strips[j].stripChar[k],p1);
					pos_cipher_blk2 = k;
					break;
				}
			}

			/*
			 * 			Position of cipher text character of block 1 - offset = position of plain text character of block 1
			 */
			pos_plain_blk2 = pos_cipher_blk2 - offset;
			if(pos_plain_blk2<0)
				pos_plain_blk2 = pos_plain_blk2 + 26;

			/*
			 * 			Selecting only those strips that are common in both blocks
			 */
			if(strips[j].stripChar[pos_plain_blk1] == tmpPlaintext[i] && strips[j].stripChar[pos_plain_blk2] == tmpPlaintext[i+25])
			{
				possibleOffset[i][l] = j;
				possibleOffset[i+25][l] = j;
				l++;
			}
		}
	}

	/*
	 * 			Finding the stips for last 2 characters of cipher text
	 */
	for(i=23; i<25; i++)
	{
		l = 0;
		for(j=0; j<100; j++)
		{
			for(k=0; k<26; k++)
			{
				if(strips[j].stripChar[k] == ciphertext[i])
				{
					pos_cipher_blk1 = k;
					break;
				}
			}

			pos_plain_blk1 = pos_cipher_blk1 - offset;
			if(pos_plain_blk1<26)
				pos_plain_blk1 = pos_plain_blk1 + 26;

			if(strips[j].stripChar[pos_plain_blk1] == tmpPlaintext[i])
			{
				possibleOffset[i][l] = j;
				l++;
			}
		}
	}

	for(i=0; i<25; i++)
	{
		stripSet[i] = possibleOffset[i][0];
		if(i==24)
			stripSet[i] = possibleOffset[i][0];
	}

	/*
	 * 			Deciphering the remaining 2 blocks of cipher text to get the entire plain text
	 */
	j=0;
	for(i=0; i<100; i++)
	{
		for(k=0; k<26; k++)
		{
			if(strips[stripSet[j]].stripChar[k] == ciphertext[i])
			{
				pos_cipher_blk1 = k;
				break;
			}
		}

		pos_plain_blk1 = pos_cipher_blk1 - offset;
		if(pos_plain_blk1<0)
			pos_plain_blk1 = pos_plain_blk1 + 26;

		plaintext[i] = strips[stripSet[j]].stripChar[pos_plain_blk1];

		j++;
		if(j % 25 == 0)
			j = 0;
	}

	/*for(i=0; i<50; i++)
	{
		printf("\nplain text character %d ------>  ",i);
		for(j=0; j<26; j++)
		{
			printf("%d\t",possibleOffset[i][j]);
		}
	}*/

	printf("Cipher text: ");
	for(i=0; i<100; i++)
	{
		printf("%c", ciphertext[i]);
	}

	printf("\nDeciphered plain text: ");
	for(i=0; i<100; i++)
	{
		printf("%c",plaintext[i]);
	}
    return 0;
}
void addOffset(int (*possibleOffset)[100], int elementNo, int *offsetCount, int offset)
{
	/*
	 * 			This function adds the offset into offset matrix
	 */
	int i = 0;
	int alreadyExist = 0;
	/*
	 * 			Search if offset already exist
	 */
	for(i=0; i<*offsetCount; i++) {
		if (possibleOffset[elementNo][i] == offset ) {
			alreadyExist = 1;
			break;
		}
	}
	/*
	 * 			Add to the offset matrix only if not already present
	 */
	if (alreadyExist == 0) {
		possibleOffset[elementNo][*offsetCount] = offset;
		*offsetCount = *offsetCount + 1;
	}
}
