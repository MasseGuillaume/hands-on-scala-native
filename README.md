# Hands-on Scala-Native

## Install Required Software

see http://www.scala-native.org/en/latest/user/setup.html

or

```
curl https://nixos.org/nix/install | sh
nix-shell .
```

## Original C program

[Nbwmon: Ncurse sbandwidth monitor](https://github.com/causes-/nbwmon)

┌[ 1.59 KiB/s ]─────────[ nbwmon-0.6 | interface: wlp4s0 ]────────────[ Received ]┐
│-              *                                                                 │
│-              *                                                           *     │
│-              *                                                           *     │
│-              *              *                                            *     │
│-              *              *                                            *     │
│-              *              *            *                               *     │
│-              *          *   *            * *                             *     │
│-              *          *   *            * *                             *    *│
│-              *          *   *   *       ****                         *   **   *│
│-              *   *      *   *   **      **** *           *           *   **   *│
│-              *   **     *   *   ***  *  **** * ** * **   *    *      *  ***   *│
└[ 0 B/s ]────────────────────────────────────────────────────────────────────────┘
┌[ 3.74 KiB/s ]────────────────────────────────────────────────────[ Transmitted ]┐
│-                                          *                                     │
│-                                          *                                     │
│-                                          *                                     │
│-                                          *                                     │
│-                                          *                                     │
│-                                          *                                     │
│-                                          *                               *     │
│-              *                           *                               *     │
│-              *          *   *            **                              *     │
│-              *          *   *   **      ****                         *   *    *│
│-              *   **     *   *   **** *  **** * ** * **   *    *  *   *  **    *│
└[ 0 B/s ]────────────────────────────────────────────────────────────────────────┘
┌[ Received ]───────────────────────────┐┌[ Transmitted ]─────────────────────────┐
│Current:                        384 B/s││Current:                         564 B/s│
│Maximum:                     1.59 KiB/s││Maximum:                      3.74 KiB/s│
│Average:                        113 B/s││Average:                         164 B/s│
│Minimum:                          0 B/s││Minimum:                           0 B/s│
│Total:                         3.65 GiB││Total:                          2.88 GiB│
│                                       ││                                        │
└───────────────────────────────────────┘└────────────────────────────────────────┘

## Running

```
sbt
> ~nativeLink
```

```
./run
```