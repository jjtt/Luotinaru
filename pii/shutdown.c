#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <unistd.h>

int main()
{
   printf("Content-Type: text/plain\n\nTrying to shutdown...");

   fflush(NULL);

   setuid( 0 );
   system( "/sbin/shutdown -h +1" );

   fflush(NULL);

   printf("OK");

   fflush(NULL);

   return 0;
}
