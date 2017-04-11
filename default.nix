let
  pkgs = import <nixpkgs> {};
  stdenv = pkgs.stdenv;
in rec {
  clangEnv = stdenv.mkDerivation rec {
    name = "clang-env";
    CLANG_PATH = pkgs.clang + "/bin/clang";
    CLANGPP_PATH = pkgs.clang + "/bin/clang++";
    buildInputs = with pkgs; [
      stdenv
      sbt
      openjdk
      boehmgc
      libunwind
      clang
    ];
  };
} 
