package Format.MACDecode;

public class linkEdit extends Data
{
  public void rebase( long pos, long end )
  {
    /*byte[] d = new byte[(int)(end - pos)];
    
    try { file.seek( pos ); Offset.setSelected( pos, end ); file.Events = false; file.read(d); } catch( java.io.IOException er ) {}

    String out = "<table border='1'><tr><td>Hex</td><td>Description.</td><td>Value</td></tr>";

    //out += "<tr><td></td><td></td><td></td></tr>";

    end -= pos; pos = 0;

    while( pos < end )
    {
      
    }

    file.Events = true;

    info("<html>Decoding of the link edit rebase information.<br /><br />" + out + "</table></html>");*/
  }

  public void bind( long pos, long end )
  {
    byte[] d = new byte[(int)(end - pos)];
    
    try { file.seek( pos ); Offset.setSelected( pos, end ); file.Events = false; file.read(d); } catch( java.io.IOException er ) {}

    String out = "<table border='1'><tr><td>Hex</td><td>Description</td><td>Value</td><td>Current location</td><td>Current name</td><td>Current Flags</td><td>Current bind type</td></tr>";

    int Pos = 0, End = d.length;

    String name = "", hex = "", bind_type = "pointer";
    long loc = 0, offset = 0;
    int opcode = 0, arg = 0, bpos = 0, flag = 0, ptr_size = is64bit ? 8 : 4;

    int bindType = 1;

    try
    {
      while( Pos < End )
      {
        opcode = d[Pos]; arg = opcode & 0x0F; opcode &= 0xF0;

        //Reset everything.

        if( opcode == 0x00 )
        {
          loc = 0; name = ""; flag = 0; bind_type = "pointer"; bindType = 1;
          out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Reset.</td><td>Opcode</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
        }

        //Set dyld ordinal.

        else if( opcode == 0x10 )
        {
          out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Set dylid(" + arg + ")</td><td>Opcode</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
        }

        //The name of the method to look up in the export of another binary.

        else if( opcode == 0x40 )
        {
          name = ""; flag = arg;
          out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Set Symbol name</td><td>Opcode</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>"; Pos += 1;
          Pos += 1; while( d[Pos] != 0x00 ) { hex += String.format("%1$02X", d[Pos] ) + " "; name += (char)d[Pos]; Pos += 1; } hex += String.format("%1$02X", d[Pos] );
          out += "<tr><td>" + hex + "</td><td>Symbol name</td><td>" + name + "</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>"; hex = "";
        }

        //Set binding type.

        else if( opcode == 0x50 )
        {
          bindType = arg; if( bindType == 1 ){ bind_type = "pointer"; } else if( bindType == 2 ) { bind_type = "relative"; } else if( bindType == 3 ) { bind_type = "absolute"; } else { bind_type = "???"; }
          out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Set Bind loc type " + bindType + ".</td><td>Opcode</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
        }


        //The segment that the method call happens.

        else if( opcode == 0x70 )
        {
          loc = segment.get( arg );
          out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Set loc to segment " + arg + "</td><td>Opcode</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
          Pos += 1;

          //The offset within the segment the pointer is at.
          //The lower 7 bits is combined as the number value as long as bit 8 is set.
          //This allows variable in length encoding of a number.

          while( d[Pos] < 0 ) { hex += String.format("%1$02X", d[Pos] ) + " "; offset |= ( d[Pos++] & 0x7F ) << bpos; bpos += 7; } 
          hex += String.format("%1$02X", d[Pos] ) + " "; offset |= d[Pos] << bpos; bpos = 0;

          loc += offset;

          out += "<tr><td>" + hex + "</td><td>loc + " + offset + "</td><td>offset = " + offset + "</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";

          offset = 0; hex = "";
        }

        //Add the current location by an offset.

        else if( opcode == 0x80 )
        {
          out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Add loc to offset</td><td>Opcode</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
          Pos += 1;

          //The offset within the segment the pointer is at.
          //The lower 7 bits is combined as the number value as long as bit 8 is set.
          //This allows variable in length encoding of a number.

          while( d[Pos] < 0 ) { hex += String.format("%1$02X", d[Pos] ) + " "; offset |= ( (long)d[Pos++] & 0x7F ) << bpos; bpos += 7; } 
          hex += String.format("%1$02X", d[Pos] ) + " "; offset |= d[Pos] << bpos; bpos = 0;

          loc += offset;

          out += "<tr><td>" + hex + "</td><td>Loc + " + offset + "</td><td>Offset = " + offset + "</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";

          offset = 0; hex = "";
        }

        //Bind the method.

        else if( opcode == 0x90 )
        {
          //After every bind we add the location by the size of the pointer.

          out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Bind method to location</td><td>Opcode (loc + " + ptr_size + ")</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";

          loc += ptr_size;
        }
      
        //Bind and add loc by offset.

        else if( opcode == 0xA0 )
        {
          out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Bind method to location</td><td>Opcode (loc + " + ptr_size + ")</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";

          Pos += 1; loc += ptr_size;

          while( d[Pos] < 0 ) { hex += String.format("%1$02X", d[Pos] ) + " "; offset |= ( (long)d[Pos++] & 0x7F ) << bpos; bpos += 7; } 
          hex += String.format("%1$02X", d[Pos] ) + " "; offset |= d[Pos] << bpos; bpos = 0;

          out += "<tr><td>" + hex + "</td><td>loc + " + offset + "</td><td>Offset = " + offset + "</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";

          loc += offset; offset = 0; hex = "";
        }

        //Bind the method.

        else if( opcode == 0xB0 )
        {
          //After every bind we add the location by the size of the pointer.

          out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Bind method to location scale = " + arg + "</td><td>Opcode (loc + " + ptr_size + " * scale + " + ptr_size + ")</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";

          arg += 1; loc += ( ptr_size ) * arg;
        }

        //Bind method number of times plus skip.

        else if( opcode == 0xC0 )
        {
          out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Number of Binds plus skip</td><td>Opcode</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
          Pos += 1;

          while( d[Pos] < 0 ) { hex += String.format("%1$02X", d[Pos] ) + " "; offset |= ( d[Pos++] & 0x7F ) << bpos; bpos += 7; } 
          hex += String.format("%1$02X", d[Pos] ) + " "; offset |= d[Pos] << bpos; bpos = 0;

          out += "<tr><td>" + hex + "</td><td>Number of binds plus skip offset " + offset + "</td><td>Count = " + offset + "</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";

          long count = offset; offset = 0; hex = ""; Pos += 1;

          while( d[Pos] < 0 ) { hex += String.format("%1$02X", d[Pos] ) + " "; offset |= ( d[Pos++] & 0x7F ) << bpos; bpos += 7; } 
          hex += String.format("%1$02X", d[Pos] ) + " "; offset |= d[Pos] << bpos; bpos = 0;

          out += "<tr><td>" + hex + "</td><td>Skip " + offset + "</td><td>Opcode</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";

          loc += ( offset + ptr_size ) * count;

          offset = 0; hex = "";
        }

        //Highly unlikely that there is an unknown opcode.

        else
        {
          out += "<tr><td>" + String.format("%1$02X", d[Pos] ) + "</td><td>Unknown Opcode.</td><td>?</td><td>" + String.format(is64bit ? "%1$016X" : "%1$08X", loc) + "</td><td>" + name + "</td><td>" + flag + "</td><td>" + bind_type + "</td></tr>";
        }

        Pos += 1;
      }
    }
    catch( Exception er ) { } //Incase the file is corrupted and we read an bad opcode that goes out of bound of the import data. This way we still load what we can.

    file.Events = true;

    info("<html>Each two hex digits is one opcode. The first hex digit is the opcode and the last hex digit is used as an 0 to 15 value.<br /><br />" +
    "<table border='1'><tr><td>Opcode</td><td>Description</td></tr>" +
    "<tr><td>4?</td><td>Sets the name for the current method. The last hex digit is the flag setting, for example 47 set the flag settings to 7.<br />" +
    "The end of the name is signified by the first value that is 00.</td></tr>" +
    "<tr><td>5?</td><td>Sets the location type. The last hex digit is used as 1 to 3 value (pointer = 1, relative = 2, or absolute = 3).<br />" +
    "Pointer means a location that is read and used as the location to the method in the program.<br />" +
    "Relative means a location that is read and added to from the current location in the code to call the method.<br />" +
    "Absolute means a location that must locate directly to the method.</td></tr>" +
    "<tr><td>7?</td><td>Sets the location to the address location of a segment load command data. The last hex digit is which segment, for example 72 would mean Seg=2.<br />" +
    "Following this opcode is a number that is added to the location in the segment.</td></tr>" +
    "<tr><td>8?</td><td>Read an number and add it to the currently set location.</td></tr>" +
    "<tr><td>9?</td><td>Use the current location, and set it to the location of the current set method name in respect to the current binding type.</td></tr>" +
    "<tr><td>A?</td><td>Use the current location, and set it to the location of the current set method name in respect to the current binding type.<br />" +
    "Additionally read a number and add it to current location.</td></tr>" +
    "<tr><td>B?</td><td>Use the current location, and set it to the location of the current set method name in respect to the current binding type.<br />" +
    "Additionally use the last hex digit and multiply it by 8 for 64 bit binaries, or 4 for 32 bit and add it to current location.</td></tr>" +
    "<tr><td>C?</td><td>Read a number for count, and a number for skip. Bind the method to the current location, then add skip to location and repeat till count number of times.<br />" +
    "In some cases we want to bind the same method to different locations evenly spaced apart number of times.</td></tr>" +
    "<tr><td>1?</td><td>Sets dylid ordinal index to. The last hex digit is used 0 to 15 ordinal.</td></tr>" +
    "<tr><td>0?</td><td>Set all current values to noting (Reset).</td></tr>" +
    "</table><br />" +
    "Each number that is read uses after an opcode uses an variable in length number encoding called ulib128.<br />" +
    "The first 7 binary digits are the value, and if the last binary digit is set one then we read the next value as the next 7 binary digits for the number.<br />" +
    "The last 7 binary digits for the number should end with a value that is smaller than 80 hex as the last binary digit should be zero.<br /><br />" +
    "After each bind opcodes 9? to C? we add 4 to the location for 32 bit binaries, or add 8 to the current location in 64 bit binaries. As that is the size of the address.<br /><br />" +
    "Lets read the opcodes and show what locations must be set to which methods.<br /><br />" +
    out + "</table></html>");
  }

  //Fully bind and decode the method calls.

  public static bind[] bindSyms( long pos, long end )
  {
    java.util.ArrayList<bind> syms = new java.util.ArrayList<bind>();

    byte[] d = new byte[(int)(end - pos)];
    
    try { file.seek( pos ); file.read(d); } catch( java.io.IOException er ) {}

    int Pos = 0, End = d.length;

    String name = "";
    long loc = 0, offset = 0;
    int opcode = 0, arg = 0, bpos = 0, ptr_size = is64bit ? 8 : 4;

    try
    {
      while( Pos < End )
      {
        opcode = d[Pos]; arg = opcode & 0x0F; opcode &= 0xF0;

        //The name of the method to look up in the export of another binary.

        if( opcode == 0x40 ) { name = ""; Pos += 1; while( d[Pos] != 0x00 ) { name += (char)d[Pos]; Pos += 1; } }

        //The segment that the method call happens.

        else if( opcode == 0x70 || opcode == 0x80 )
        {
          if( opcode == 0x70 ) { loc = segment.get( arg ); }

          Pos += 1; while( d[Pos] < 0 ) { offset |= ( (long)d[Pos++] & 0x7F ) << bpos; bpos += 7; } offset |= d[Pos] << bpos; bpos = 0;

          loc += offset; offset = 0;
        }

        //Bind the method opcodes.

        else if( opcode >= 0x90 && opcode <= 0xC0 )
        {
          //Bind the method.

          if( opcode == 0x90 ) { syms.add( new bind( loc, name ) ); }

          //Bind and add loc by offset.

          else if( opcode == 0xA0 )
          {
            syms.add( new bind( loc, name ) );

            //After every bind we add the location by the size of the pointer.

            Pos += 1; while( d[Pos] < 0 ) { offset |= ( (long)d[Pos++] & 0x7F ) << bpos; bpos += 7; } offset |= d[Pos] << bpos; bpos = 0;

            loc += offset; offset = 0;
          }

          //Bind the method with an scaled pointer.

          else if( opcode == 0xB0 ) { syms.add( new bind( loc, name ) ); loc += ptr_size * arg; }

          //Bind the method skip bytes till count times.

          else if( opcode == 0xC0 )
          {
            //count.

            Pos += 1; while( d[Pos] < 0 ) { offset |= ( (long)d[Pos++] & 0x7F ) << bpos; bpos += 7; } offset |= d[Pos] << bpos; bpos = 0;

            long count = offset - 1; offset = 0;

            //bytes to Skip.

            Pos += 1; while( d[Pos] < 0 ) { offset |= ( (long)d[Pos++] & 0x7F ) << bpos; bpos += 7; } offset |= d[Pos] << bpos; bpos = 0;

            //Bind the method while skipping bytes.

            for( int i = 0; i < count; i++ ) { syms.add( new bind( loc, name ) ); loc += offset + ptr_size; }

            if( count >= 0 ) { syms.add( new bind( loc, name ) ); loc += offset; } offset = 0;
          }

          //After every bind we add the location by the size of the pointer.

          loc += ptr_size;
        }

        //Reset everything.

        else if( opcode == 0x00 ) { loc = 0; name = ""; }

        Pos += 1;
      }
    }
    catch( Exception er ) { } //Incase the file is corrupted and we read an bad opcode that goes out of bound of the import data. This way we still load what we can.

    return( syms.toArray( new bind[ syms.size() ] ) );
  }

  public void export( long pos, long end )
  {
    /*byte[] d = new byte[(int)(end - pos)];
    
    try { file.seek( pos ); Offset.setSelected( pos, end ); file.Events = false; file.read(d); } catch( java.io.IOException er ) {}

    String out = "<table border='1'><tr><td>Hex</td><td>Description.</td><td>Value</td></tr>";

    end -= pos; pos = 0;

    while( pos < end )
    {
      
    }

    file.Events = true;

    info("<html>Decoding of the link edit export information.<br /><br />" + out + "</table></html>");*/
  }
}
