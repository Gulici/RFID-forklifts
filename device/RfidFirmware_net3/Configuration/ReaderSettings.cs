using System.Collections.Generic;

namespace RfidFirmware.Configuration
{
    public class ReaderSettings
    {
        public string ReaderPort { get; set; }

        public List<int> Power { get; set; } = new List<int>();
        public List<bool> EnableAntennas { get; set; } = new List<bool>();
        public List<bool> AntennasForGpios1_3 { get; set; } = new List<bool>();

        public List<bool> AntennasForGpio4 { get; set; } = new List<bool>();

        public int Timeout { get; set; }

        public string TagMask { get; set; }

        public string TagEpc_1 { get; set; }

        public string TagEpc_2 { get; set; }

        public string TagEpc_3 { get; set; }

        public string TagEpc_4 { get; set; }

        public string TagEpc_5 { get; set; }

        public string TagEpc_6 { get; set; }
    }
}
