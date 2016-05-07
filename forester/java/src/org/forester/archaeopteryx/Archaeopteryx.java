// $Id:
// FORESTER -- software libraries and applications
// for evolutionary biology research and applications.
//
// Copyright (C) 2008-2009 Christian M. Zmasek
// Copyright (C) 2008-2009 Burnham Institute for Medical Research
// All rights reserved
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
//
// Contact: phylosoft @ gmail . com
// WWW: https://sites.google.com/site/cmzmasek/home/software/forester

package org.forester.archaeopteryx;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.JCommander;

import java.io.File;

import org.forester.io.parsers.PhylogenyParser;
import org.forester.io.parsers.nexus.NexusPhylogeniesParser;
import org.forester.io.parsers.nhx.NHXParser;
import org.forester.io.parsers.phyloxml.PhyloXmlParser;
import org.forester.io.parsers.util.ParserUtils;
import org.forester.phylogeny.Phylogeny;
import org.forester.phylogeny.PhylogenyMethods;
import org.forester.util.ForesterUtil;

public final class Archaeopteryx {

    public static MainFrame createApplication( final Phylogeny phylogeny ) {
        final Phylogeny[] phylogenies = new Phylogeny[ 1 ];
        phylogenies[ 0 ] = phylogeny;
        return createApplication( phylogenies, "", "" );
    }

    public static MainFrame createApplication( final Phylogeny phylogeny, final Configuration config, final String title ) {
        final Phylogeny[] phylogenies = new Phylogeny[ 1 ];
        phylogenies[ 0 ] = phylogeny;
        return MainFrameApplication.createInstance( phylogenies, config, title );
    }

    public static MainFrame createApplication( final Phylogeny[] phylogenies ) {
        return createApplication( phylogenies, "", "" );
    }

    public static MainFrame createApplication( final Phylogeny[] phylogenies,
                                               final String config_file_name,
                                               final String title ) {
        return MainFrameApplication.createInstance( phylogenies, config_file_name, title );
    }

    @Parameter(names={"--configfile", "-c"})
    String configfile;
    @Parameter(names={"--treefile", "-f"})
    String treefile;
 
    public static void main(String ... args) {
        Archaeopteryx main = new Archaeopteryx();
        new JCommander(main, args);
        main.run();
    }
 
    public void run() {
        System.out.printf("Config file: %s\n", configfile);
        System.out.printf("Tree file: %s\n", treefile);
    
        Phylogeny[] phylogenies = null;
        Configuration conf = new Configuration( null, false, false, true );
        File f = null;
        try {
            if (configfile != "") {
                conf = new Configuration( configfile, false, false, true );
            }
            if (treefile != "") {
                f = new File( treefile );
                final String err = ForesterUtil.isReadableFile( f );
                if ( !ForesterUtil.isEmpty( err ) ) {
                    ForesterUtil.fatalError( Constants.PRG_NAME, err );
                }
                boolean nhx_or_nexus = false;
                final PhylogenyParser p = ParserUtils.createParserDependingOnFileType( f, conf
                                                                                       .isValidatePhyloXmlAgainstSchema() );
                if ( p instanceof NHXParser ) {
                    nhx_or_nexus = true;
                    final NHXParser nhx = ( NHXParser ) p;
                    nhx.setReplaceUnderscores( conf.isReplaceUnderscoresInNhParsing() );
                    nhx.setIgnoreQuotes( false );
                    nhx.setTaxonomyExtraction( conf.getTaxonomyExtraction() );
                }
                else if ( p instanceof NexusPhylogeniesParser ) {
                    nhx_or_nexus = true;
                    final NexusPhylogeniesParser nex = ( NexusPhylogeniesParser ) p;
                    nex.setReplaceUnderscores( conf.isReplaceUnderscoresInNhParsing() );
                    nex.setIgnoreQuotes( false );
                }
                else if ( p instanceof PhyloXmlParser ) {
                    MainFrameApplication.warnIfNotPhyloXmlValidation( conf );
                }
                phylogenies = PhylogenyMethods.readPhylogenies( p, f );
                if ( nhx_or_nexus && conf.isInternalNumberAreConfidenceForNhParsing() ) {
                    for( final Phylogeny phy : phylogenies ) {
                        PhylogenyMethods.transferInternalNodeNamesToConfidence( phy, "" );
                    }
                }
            }
        }
        catch ( final Exception e ) {
            ForesterUtil.fatalError( Constants.PRG_NAME, "failed to start: " + e.getLocalizedMessage() );
        }
        String title = "";
        if ( f != null ) {
            title = f.getName();
        }
        File current_dir = null;
        if ( ( phylogenies != null ) && ( phylogenies.length > 0 ) ) {
            current_dir = new File( "." );
        }
        try {
            MainFrameApplication.createInstance( phylogenies, conf, title, current_dir );
        }
        catch ( final OutOfMemoryError e ) {
            AptxUtil.outOfMemoryError( e );
        }
        catch ( final Exception e ) {
            AptxUtil.unexpectedException( e );
        }
        catch ( final Error e ) {
            AptxUtil.unexpectedError( e );
        }
    }
}