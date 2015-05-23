#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <unistd.h>

int main()
{
   setuid( 0 );
   system( "/sbin/shutdown -h +1" );

   printf("Content-Type: text/plain\n\nOK");

   return 0;
}
