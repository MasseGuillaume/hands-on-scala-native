/*
  CURHELLO.C
  ==========
  (c) Copyright Paul Griffiths 1999
  Email: mail@paulgriffiths.net

  "Hello, world!", ncurses style.
*/

#include <stdlib.h>
#include <stdio.h>
#include <unistd.h> /* for sleep() */
#include <curses.h>

int main(void) {


  /*  Initialize ncurses  */

  if ( (mainwin = initscr()) == NULL ) {
    fprintf(stderr, "Error initialising ncurses.\n");
    exit(EXIT_FAILURE);
  }

  /*  Display "Hello, world!" in the center of the
  screen, call refresh() to show our changes, and
  sleep() for a few seconds to get the full screen effect  */

  mvaddstr(13, 33, "Hello, world!");
  refresh();
  sleep(3);


  /*  Clean up after ourselves  */

  delwin(mainwin);
  endwin();
  refresh();

  return EXIT_SUCCESS;
}