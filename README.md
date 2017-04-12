# Hands-on Scala-Native

## Install Required Software

see http://www.scala-native.org/en/latest/user/setup.html

or

```
curl https://nixos.org/nix/install | sh
nix-shell .
```

## 1. The extern keyword

```
cd extern
clang -c lib.c
cd ..

sbt extern/run
```

## 2. Stackalloc / AnyVal

```
sbt stackalloc/run

cd stackalloc
clang stackalloc.c
./a.out
cd ..
```

## 3. The link keyword

The original c program

```
cd ncurses
clang -lncurses CURHELLO.C
./a.out 
(ctrl+c to exit)
cd ..
```

## 4. Debuging

TODO create segfault demo

```
lldb nbwmon/target/scala-2.11/nbwmon-out
> run
> bt
```