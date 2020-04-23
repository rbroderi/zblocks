package zblocks.Blocks.Interfaces;

public interface Matchable {

	
	public boolean matches(Matchable other);
	public Class<?> getMatchType();
	public Object getTrait();
}
