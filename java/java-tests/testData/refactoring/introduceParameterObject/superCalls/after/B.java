class B extends Test {
  void foo(Param param) {
    super.foo(param);
    System.err.println(param.setI(param.getI() - 1));
  }

  void bar() {
    foo(new Param(1));
  }
}