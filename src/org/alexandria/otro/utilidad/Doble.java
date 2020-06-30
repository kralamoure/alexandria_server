package org.alexandria.otro.utilidad;

public class Doble<L, R>
{
  public L primero;
  public R segundo;

  public Doble(L primero, R segundo)
  {
    this.primero = primero;
    this.segundo = segundo;
  }

  public L getPrimero()
  {
    return primero;
  }
  public R getSegundo()
  {
    return segundo;
  }

  @Override
  public int hashCode()
  {
    return primero.hashCode()^ segundo.hashCode();
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean equals(Object o)
  {
    if(!(o instanceof Doble))
      return false;
    Doble<L, R> pairo=(Doble<L, R>)o;
    return this.primero.equals(pairo.getPrimero())&&this.segundo.equals(pairo.getSegundo());
  }

}