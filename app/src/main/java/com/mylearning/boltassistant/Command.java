package com.mylearning.boltassistant;
 /*
                                         _____ Interface ___
         ---------------                |    Command        |
         | invoker      |               |-------------------
         ----------------  <>---------->|  execute()        |
         |here is st     |              |                   |
         |     and rt    |               -------------------
          ---------------                         ^
                                                  |
          ----------------                        |
          |  Reciver     |                -------------------
          ----------------  <-------------|  concrete command |
          |               |               --------------------
          |---------------|               |                   |
                                          ---------------------
  */
public interface Command {
    void execute();
}
