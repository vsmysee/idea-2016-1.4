class Foo:
    def foo(self):
        print("a")

class Zope(Foo):
    def _mine(self):
       print "zope"

class Boo(Zope):
    def boo(self):
        print "rrrrr"