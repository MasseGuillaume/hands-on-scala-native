#include <time.h>
#include <sys/time.h>
#include <stdlib.h>
#include <stdio.h>

int main(void) {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  printf("time: %d(s) %d(us)", tv.tv_sec, tv.tv_usec);
  return EXIT_SUCCESS;
}