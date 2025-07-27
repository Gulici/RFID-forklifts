using System.ComponentModel.DataAnnotations;

namespace RfidFirmware.Configuration
{
    public class ReaderSettings
    {   
        [Required]
        public required string ReaderPort { get; set; }
        [Required]
        public required List<int> Power { get; set; }
        [Required]
        public required List<bool> EnableAntennas { get; set; }
        [Required]
        public required List<bool> AntennasForGpios1_3 { get; set; }
        [Required]
        public required List<bool> AntennasForGpio4 { get; set; }
        [Required]
        public required int Timeout { get; set; }
        [Required]
        public required string TagMask { get; set; }
        [Required]
        public required string TagEpc_1 { get; set; }
        [Required]
        public required string TagEpc_2 { get; set; }
        [Required]
        public required string TagEpc_3 { get; set; }
        [Required]
        public required string TagEpc_4 { get; set; }
        [Required]
        public required string TagEpc_5 { get; set; }
        [Required]
        public required string TagEpc_6 { get; set; }
    }
}