let
  pkgs = import <nixpkgs> {};
  stdenv = pkgs.stdenv;
in rec {
  clangEnv = stdenv.mkDerivation rec {
    name = "clang-env";
    CLANG_PATH = pkgs.clang + "/bin/clang";
    CLANGPP_PATH = pkgs.clang + "/bin/clang++";
    buildInputs = with pkgs; [

      # for scala-native
      stdenv
      sbt
      openjdk
      boehmgc
      libunwind
      re2
      clang
      lldb

      # for this demo
      ncurses
    ];
  };
} 
