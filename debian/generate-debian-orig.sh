#!/bin/sh

# all directories are relative to the current dir

tarballs="corba.tar.bz2 hotspot.tar.bz2 jaxp.tar.bz2 jaxws.tar.bz2 jdk.tar.bz2 langtools.tar.bz2 openjdk.tar.bz2"
tarballs="$tarballs icedtea-sound-1.0.1.tar.xz"
jamvmtb=jamvm-2.0.0.tar.gz

# tarballs location
tarballdir=7u241

# icedtea upstream location (as extracted from icedtea's tarball)
icedtea_checkout=icedtea-2.6.20

# openjdk's debian location (usually fetched from bzr or the latest openjdk)
debian_checkout=openjdk-7

base=openjdk-7
version=7u241-2.6.20

# output directory
pkgdir=$base-$version

# new orig file
origtar=${base}_${version}.orig.tar.gz

if [ -d $pkgdir ]; then
    echo directory $pkgdir already exists
    exit 1
fi

if [ -d $pkgdir.orig ]; then
    echo directory $pkgdir.orig already exists
    exit 1
fi

if [ -f $origtar ]; then
    echo "Using existing $origtar"
    tar xf $origtar
    if [ -d $pkgdir.orig ]; then
       mv $pkgdir.orig $pkgdir
    fi
    tar -c -f - -C $icedtea_checkout . | tar -x -f - -C $pkgdir
    rm -rf $pkgdir/.hg
else
    echo "Creating new $pkgdir.orig/"
    rm -rf $pkgdir.orig
    mkdir -p $pkgdir.orig
    case "$base" in
      openjdk*)
        for i in $tarballs; do
            cp -p $tarballdir/$i $pkgdir.orig/
        done
        cp -p $tarballdir/$jamvmtb $pkgdir.orig/
      ;;
    esac
    tar -c -f - -C $icedtea_checkout . | tar -x -f - -C $pkgdir.orig
    (
      cd $pkgdir.orig
      sh autogen.sh
      rm -rf autom4te.cache
    )
    cp -a $pkgdir.orig $pkgdir.new
    rm -rf $pkgdir.orig/.hg
    mv $pkgdir.orig $pkgdir
    tar cfz $origtar $pkgdir
    rm -rf $pkgdir
    mv $pkgdir.new $pkgdir
fi

echo "Build debian diff in $pkgdir/"
cp -a $debian_checkout $pkgdir/debian
(
  cd $pkgdir
  #bash debian/update-shasum.sh
  #bash debian/update-hgrev.sh
  ls
  sh autogen.sh
  rm -rf autom4te.cache
)
