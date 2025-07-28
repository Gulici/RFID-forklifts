using System;

namespace RfidFirmware.Models
{
    public class DeviceLocationDto
    {
        public Guid Id { get; set; }
        public Guid FirmId { get; set; }
        public string EpcCode { get; set; }
    }
}