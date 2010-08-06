PREFIX?=$(HOME)/.usr
DATADIR?=$(PREFIX)/share
LIBEXECDIR?=$(PREFIX)/libexec

PKG_CONFIG?=pkg-config
ANT?=ant

JMETUX_CLASSPATH=`$(PKG_CONFIG) --variable=classpath jar.metux-java`
ANT_FLAGS+=-Ddestdir=$(DESTDIR) -Dprefix=$(PREFIX) -Ddatadir=$(DATADIR) -Dlibexecdir=$(LIBEXECDIR) -DJMETUX_CLASSPATH=$(JMETUX_CLASSPATH)

all:	compile

compile:
	$(ANT) $(ANT_FLAGS) compile

install:
	$(ANT) $(ANT_FLAGS) install
