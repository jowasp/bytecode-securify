package the.bytecode.club.bytecodeviewer.xposedgenerator;

import static org.junit.Assert.*;

import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;

import junit.framework.TestCase;

public class XposedGeneratorTest {

	public class test extends TestCase {
		
		public void testValuesNull(){
			
			String classname =null;
			String containerName = null;
			ClassNode classNode = null;
			XposedGenerator addend = new XposedGenerator();			
			addend.ParseChosenFileContent(classname, containerName, classNode);
			//assertNotNull(addend.ParseChosenFileContent(classname, containerName, classNode));
		}
		
	}
}


